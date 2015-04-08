package org.levelspace

import java.awt.BorderLayout.CENTER
import java.awt.{BorderLayout, GridLayout, FlowLayout}
import java.awt.TextField
import java.awt.event.KeyEvent.{VK_ENTER, VK_SHIFT, VK_ESCAPE}
import java.awt.event.{TextEvent, TextListener}
import java.awt.Dimension
import javax.swing._
import javax.swing.SpringLayout._
import javax.swing.event.{DocumentListener, DocumentEvent}

import org.nlogo.api.Dump
import org.nlogo.api.SimpleJobOwner
import org.nlogo.api.NumberParser
import org.nlogo.api.CompilerException
import org.nlogo.api.Observer
import org.nlogo.api.LogoList
import org.nlogo.app.EditorFactory
import org.nlogo.awt.EventQueue.invokeLater
import org.nlogo.window.GUIWorkspace

import javax.swing.{JScrollPane, JTextArea, JEditorPane, JTextField}
import javax.swing.KeyStroke.getKeyStroke
import uk.ac.surrey.xw.api.DoubleProperty
import uk.ac.surrey.xw.api._
import uk.ac.surrey.xw.api.swing.enrichComponent
import uk.ac.surrey.xw.api.swing.newAction
import uk.ac.surrey.xw.api.swing.enrichJButton
import uk.ac.surrey.xw.api.swing.enrichItemSelectable

import net.miginfocom.swing._

class ProcedureWidgetKind[W <: ProcedureWidget] extends LabeledPanelWidgetKind[W] {
  val newWidget = new ProcedureWidget(_, _, _)
  val name = "PROCEDURE-WIDGET"
  val codeProperty = new StringProperty[W]("CODE",
    Some((w, s) ⇒ { w.code = s; w.editor.setText(s) }), _.code)
  val nameProperty = new StringProperty[W]("NAME",
    Some((w, s) => { w.nameField.setText(s) }), _.nameField.getText)
  val argProperty = new StringProperty[W]("ARGS",
    Some((w, s) => { w.argField.setText(s) }), _.argField.getText)
  val saveProperty = new StringProperty[W]("SAVE-COMMAND",
    Some(_.saveCommand = _), _.saveCommand)
  val deleteProperty = new StringProperty[W]("DELETE-COMMAND",
    Some(_.deleteCommand = _), _.deleteCommand)
  val defaultProperty = Some(codeProperty)
  override def propertySet = super.propertySet ++ Set(codeProperty, nameProperty, argProperty, saveProperty, deleteProperty)
}

class ProcedureWidget(val key: WidgetKey, val state: State, val ws: GUIWorkspace) extends LabeledPanelWidget {
  val owner = new SimpleJobOwner(key, ws.world.mainRNG, classOf[Observer]) {
    override def isButton = true
    override def ownsPrimaryJobs = true
  }
  var saveCommand = ""
  var deleteCommand = ""

  override val kind = new ProcedureWidgetKind[this.type]
  var code = ""
  val editor: org.nlogo.window.CodeEditor = new EditorFactory(ws.world.compiler).newEditor(0,0, true,
    new TextListener {
      override def textValueChanged(e: TextEvent): Unit = {
        code = editor.getText
        updateInState(kind.codeProperty)
      }
    },
    false
  )
  val springLayout = new SpringLayout()
  setLayout(springLayout)
  val bigSpace = 5
  val smallSpace = 3

  val nameLabel = new JLabel("Name:")
  springLayout.putConstraint(WEST, nameLabel, bigSpace, WEST, this)
  springLayout.putConstraint(NORTH, nameLabel, bigSpace, NORTH, this)
  add(nameLabel)

  val nameField = new JTextField(10)
  bindToProperty(nameField, kind.nameProperty)
  springLayout.putConstraint(WEST, nameField, smallSpace, EAST, nameLabel)
  springLayout.putConstraint(NORTH, nameField, smallSpace, NORTH, this)
  //springLayout.putConstraint(EAST, nameField, -bigSpace, EAST, this)
  add(nameField)

  val saveButton = new JButton("save")
  saveButton.onActionPerformed(_ =>
    try ws.evaluateCommands(owner, saveCommand, ws.world.observers, false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  )
  springLayout.putConstraint(EAST, nameField, -bigSpace, WEST, saveButton)
  springLayout.putConstraint(NORTH, saveButton, bigSpace, NORTH, this)
  springLayout.putConstraint(EAST, saveButton, -bigSpace, EAST, this)
  add(saveButton)

  val argLabel = new JLabel("Argument names:")
  springLayout.putConstraint(WEST, argLabel, bigSpace, WEST, this)
  springLayout.putConstraint(NORTH, argLabel, bigSpace+bigSpace, SOUTH, nameLabel)
  add(argLabel)

  val argField = new JTextField()
  bindToProperty(argField, kind.argProperty)
  springLayout.putConstraint(WEST, argField, smallSpace, EAST, argLabel)
  springLayout.putConstraint(NORTH, argField, smallSpace, SOUTH, nameField)
  springLayout.putConstraint(EAST, argField, -bigSpace, EAST, this)
  add(argField)

  val deleteButton = new JButton("delete")
  deleteButton.onActionPerformed(_ =>
    try ws.evaluateCommands(owner, deleteCommand, ws.world.observers, false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  )
  springLayout.putConstraint(EAST, argField, -bigSpace, WEST, deleteButton)
  springLayout.putConstraint(NORTH, deleteButton, bigSpace, SOUTH, saveButton)
  springLayout.putConstraint(EAST, deleteButton, -bigSpace, EAST, this)
  add(deleteButton)

  val scrollPane = new JScrollPane(editor)
  springLayout.putConstraint(NORTH, scrollPane, bigSpace, SOUTH, argLabel)
  springLayout.putConstraint(NORTH, scrollPane, bigSpace, SOUTH, argField)
  springLayout.putConstraint(WEST, scrollPane, bigSpace, WEST, this)
  springLayout.putConstraint(SOUTH, scrollPane, -bigSpace, SOUTH, this)
  springLayout.putConstraint(EAST, scrollPane, -bigSpace, EAST, this)
  add(scrollPane)

  def bindToProperty(field: JTextField, property: StringProperty[this.type]) =
    field.getDocument().addDocumentListener(new DocumentListener {
      override def changedUpdate(e: DocumentEvent): Unit = updateInState(property)
      override def removeUpdate(e: DocumentEvent): Unit = updateInState(property)
      override def insertUpdate(e: DocumentEvent): Unit = updateInState(property)
    })
}

class RelationshipKind[W <: Relationship] extends JComponentWidgetKind[W] {
  override val name = "RELATIONSHIP"
  override val newWidget = new Relationship(_, _, _)

  val selectedAgentReporterProperty = new StringProperty[W]("SELECTED-AGENT-REPORTER",
    Some(_.selectedAgentReporter = _), _.selectedAgentReporter)

  val availableAgentReporterProperty = new ListProperty[W]("AVAILABLE-AGENT-REPORTERS",
    Some((w,o) => w.availableAgentReporters = o.toVector),
    w => LogoList.fromIterator(w.availableAgentReporters.iterator))

  val selectedProcedureProperty = new StringProperty[W]("SELECTED-PROCEDURE",
    Some(_.selectedProcedure = _), _.selectedProcedure)

  val availableProceduresProperty = new ListProperty[W]("AVAILABLE-PROCEDURES",
    Some((w,o) => w.availableProcedures = o.toVector),
    w => LogoList.fromIterator(w.availableProcedures.iterator))

  override val defaultProperty = None
  override def propertySet = super.propertySet ++ Set(
    selectedAgentReporterProperty,
    availableAgentReporterProperty,
    selectedProcedureProperty,
    availableProceduresProperty
  )
}

class Relationship(val key: WidgetKey, val state: State, val ws: GUIWorkspace) extends JPanel with JComponentWidget {
  override val kind = new RelationshipKind[this.type]

  /*
  var _selectedAgentReporter = ""
  var _availableAgentReporters = Seq.empty[String]
  var _agentArgumentNames = Seq.empty[String]
  var _selectedAgentArguments = Seq.empty[String]
  var _availableAgentArguments = Seq.empty[Seq[String]]
  var _selectedProcedure = ""
  var _availableProcedures = Seq.empty[String]
  var _procedureArgumentNames = Seq.empty[String]
  var _procedureSelectedArguments = Seq.empty[String]
  var _procedureAvailableArguments = Seq.empty[Seq[String]]
*/

  removeAll()
  setLayout(new MigLayout("insets 5"))
  add(new JLabel("ask"), "align right")
  val agentSelector = new JComboBox()
  agentSelector.onItemStateChanged((e) => updateInState(kind.selectedAgentReporterProperty))
  add(agentSelector, "grow, wrap")

  val agentArgumentPanel = new JPanel()
  add(agentArgumentPanel, "grow, span, wrap")

  add(new JLabel("to do"), "align right")
  val procedureSelector = new JComboBox()
  procedureSelector.onItemStateChanged((e) => updateInState(kind.selectedProcedureProperty))
  add(procedureSelector, "grow, wrap")

  val procedureArgumentPanel = new JPanel()
  add(procedureArgumentPanel, "grow, span, wrap")

  def selectedAgentReporter = Option(agentSelector.getSelectedItem).map(_.toString).getOrElse("")
  def selectedAgentReporter_= (item: String) = agentSelector.setSelectedItem(item: AnyRef)
  def availableAgentReporters = (0 until agentSelector.getItemCount).map(agentSelector.getItemAt _)
  def availableAgentReporters_= (items: Iterable[AnyRef]) = {
    agentSelector.setModel(new DefaultComboBoxModel(items.toArray: Array[AnyRef]))
    updateInState(kind.selectedAgentReporterProperty)
  }

  def selectedProcedure = Option(procedureSelector.getSelectedItem).map(_.toString).getOrElse("")
  def selectedProcedure_= (item: String) = procedureSelector.setSelectedItem(item: AnyRef)
  def availableProcedures = (0 until procedureSelector.getItemCount).map(procedureSelector.getItemAt _)
  def availableProcedures_= (items: Iterable[AnyRef]) = {
    procedureSelector.setModel(new DefaultComboBoxModel(items.toArray: Array[AnyRef]))
    updateInState(kind.selectedProcedureProperty)
  }

}

