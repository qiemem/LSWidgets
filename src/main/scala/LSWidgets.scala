package org.levelspace

import java.awt.event.{ItemEvent, TextEvent, TextListener}
import javax.swing.SpringLayout._
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing._

import net.miginfocom.swing._
import org.nlogo.api.{CompilerException, LogoList, Observer, SimpleJobOwner}
import org.nlogo.app.EditorFactory
import org.nlogo.window.GUIWorkspace
import uk.ac.surrey.xw.api._
import uk.ac.surrey.xw.api.swing.{enrichItemSelectable, enrichJButton}

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
  override def propertySet = super.propertySet ++ Set(codeProperty, nameProperty, argProperty,
    saveProperty, deleteProperty)
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

  val saveButton = new JButton("save")
  saveButton.onActionPerformed(_ =>
    try ws.evaluateCommands(owner, saveCommand, ws.world.observers, false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  )
  springLayout.putConstraint(NORTH, saveButton, bigSpace, NORTH, this)
  springLayout.putConstraint(WEST, saveButton, bigSpace, WEST, this)
  add(saveButton)

  val deleteButton = new JButton("delete")
  deleteButton.onActionPerformed(_ =>
    try ws.evaluateCommands(owner, deleteCommand, ws.world.observers, waitForCompletion = false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  )
  springLayout.putConstraint(NORTH, deleteButton, bigSpace, NORTH, this)
  springLayout.putConstraint(WEST, deleteButton, bigSpace, EAST, saveButton)
  add(deleteButton)

  val nameLabel = new JLabel("Name:")
  springLayout.putConstraint(WEST, nameLabel, bigSpace, WEST, this)
  springLayout.putConstraint(NORTH, nameLabel, bigSpace, SOUTH, saveButton)
  springLayout.putConstraint(NORTH, nameLabel, bigSpace, SOUTH, deleteButton)
  add(nameLabel)

  val nameField = new JTextField(10)
  bindToProperty(nameField, kind.nameProperty)
  springLayout.putConstraint(WEST, nameField, smallSpace, EAST, nameLabel)
  springLayout.putConstraint(EAST, nameField, -bigSpace, EAST, this)
  springLayout.putConstraint(NORTH, nameField, bigSpace, SOUTH, saveButton)
  springLayout.putConstraint(NORTH, nameField, bigSpace, SOUTH, deleteButton)
  add(nameField)

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
  import org.levelspace.Enhancer._

  override val name = "RELATIONSHIP"
  override val newWidget = new Relationship(_, _, _)

  val selectedAgentReporterProperty = new StringProperty[W]("SELECTED-AGENT-REPORTER",
    Some((w,s) => w.agentSelector.selectedItem = s), _.agentSelector.selectedItem)

  val availableAgentReporterProperty = new ListProperty[W]("AVAILABLE-AGENT-REPORTERS",
    Some((w,l) => w.agentSelector.items = l.scalaIterator.toSeq), _.agentSelector.items.toLogo)

  val selectedProcedureProperty = new StringProperty[W]("SELECTED-PROCEDURE",
    Some((w,s) => w.procedureSelector.selectedItem = s), _.procedureSelector.selectedItem)

  val availableProceduresProperty = new ListProperty[W]("AVAILABLE-PROCEDURES",
    Some((w,l) => w.procedureSelector.items = l.toVector), _.procedureSelector.items.toLogo)

  val saveCommandProperty = new StringProperty[W]("SAVE-COMMAND",
    Some(_.saveCommand = _), _.saveCommand)

  val deleteCommandProperty = new StringProperty[W]("DELETE-COMMAND",
    Some(_.deleteCommand = _), _.deleteCommand)

  val selectedProcedureArguments = new ListProperty[W]("SELECTED-PROCEDURE-ARGUMENTS",
    Some((w, l) => w.selectedProcedureArguments = l), _.selectedProcedureArguments)

  val availableProcedureArguments = new ListProperty[W]("AVAILABLE-PROCEDURE-ARGUMENTS",
    Some((w,l) => w.availableProcedureArguments = l), _.availableProcedureArguments)

  override val defaultProperty = None
  override def propertySet = super.propertySet ++ Set(
    selectedAgentReporterProperty,
    availableAgentReporterProperty,
    selectedProcedureProperty,
    availableProceduresProperty,
    saveCommandProperty,
    deleteCommandProperty,
    selectedProcedureArguments,
    availableProcedureArguments
  )
}

class Relationship(val key: WidgetKey, val state: State, val ws: GUIWorkspace) extends JPanel with JComponentWidget {
  import org.levelspace.Enhancer._

  override val kind = new RelationshipKind[this.type]
  val owner = new SimpleJobOwner(key, ws.world.mainRNG, classOf[Observer]) {
    override def isButton = true
    override def ownsPrimaryJobs = true
  }

  var procedureArguments = Map.empty[String, XWComboBox]

  var saveCommand = ""
  var deleteCommand = ""

  removeAll()
  setLayout(new MigLayout("insets 5"))
  add(new JLabel("extended agentset"), "align right")
  val agentSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedAgentReporterProperty))
  add(agentSelector, "grow, wrap")

  val agentArgumentPanel = new JPanel()
  add(agentArgumentPanel, "grow, span, wrap")

  add(new JLabel("commands"), "align right")
  val procedureSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedProcedureProperty))
  procedureSelector.onItemStateChanged { event ⇒
    if (event.getStateChange == ItemEvent.SELECTED)
      updateInState(kind.selectedProcedureProperty)
  }
  add(procedureSelector, "grow, wrap")

  val procedureArgumentPanel = new JPanel()
  add(procedureArgumentPanel, "grow, span, wrap")

  val buttonPanel = new JPanel()
  val saveButton = new JButton("save")
  buttonPanel.add(saveButton)
  saveButton.onActionPerformed(_ =>
    try ws.evaluateCommands(owner, saveCommand, ws.world.observers, waitForCompletion = false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  )

  val deleteButton = new JButton("delete")
  buttonPanel.add(deleteButton)
  deleteButton.onActionPerformed(_ =>
    try ws.evaluateCommands(owner, deleteCommand, ws.world.observers, waitForCompletion = false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  )

  add(buttonPanel, "grow, span")

  def selectedProcedureArguments =
    procedureArguments.map {
      case (name: String, comboBox: XWComboBox) => LogoList(name, comboBox.selectedItem)
    }.toSeq.toLogo

  def selectedProcedureArguments_= (args: LogoList): Unit = args.foreach {
    case arg: LogoList =>
      val name = arg.get(0).asInstanceOf[String]
      val value = arg.get(1).asInstanceOf[String]
      procedureArguments(name).selectedItem = value
    case x =>
      ws.warningMessage("Invalid selection item: " + x.toString)
  }

  def availableProcedureArguments =
    procedureArguments.map {
      case (name: String, comboBox: XWComboBox) => LogoList(name, comboBox.items.toLogo)
    }.toSeq.toLogo

  def availableProcedureArguments_= (list: LogoList) = {
    val (names, items) = list.map(_.asInstanceOf[LogoList]).map { l =>
      l.get(0).toString -> l.get(1).asInstanceOf[LogoList].map(_.toString)
    }.toSeq.unzip
    procedureArguments = names.zip(items.map {
      opts =>
        val chooser: XWComboBox = new XWComboBox(() => Relationship.this.updateInState(kind.selectedProcedureArguments))
        chooser.items = opts.toSeq
        chooser
    }).toMap

    procedureArgumentPanel.removeAll()
    procedureArgumentPanel.setLayout(new MigLayout())
    procedureArguments.foreach {
      case (name: String, selector: XWComboBox) =>
        procedureArgumentPanel.add(new JLabel(name))
        procedureArgumentPanel.add(selector, "grow, wrap")
        selector.onItemStateChanged { event =>
          if (event.getStateChange == ItemEvent.SELECTED)
            updateInState(kind.selectedProcedureArguments)
        }
        if (selector.getItemCount > 0) selector.setSelectedIndex(0)
        updateInState(kind.selectedProcedureArguments)
    }
    procedureArgumentPanel.revalidate()
  }


}

class XWComboBox(selectionCallback: ()=>Unit) extends JComboBox {
  def selectedItem: String = Option(getSelectedItem).map(_.toString).getOrElse("")

  def selectedItem_=(item: String) = setSelectedItem(item)

  def items: Seq[String] = (0 until getItemCount).map(getItemAt(_).toString)

  def items_=(items: Seq[AnyRef]) = {
    removeAllItems()
    items.foreach(addItem)
    setSelectedItem(items.headOption.orNull)
  }

  this.onItemStateChanged { event =>
    if (event.getStateChange == ItemEvent.SELECTED) selectionCallback()
  }
}


object Enhancer {
  case class LogoSeq(seq: Seq[AnyRef]) {
    def toLogo: LogoList = LogoList.fromIterator(seq.iterator)
  }

  implicit def toLogoSeq(seq: Seq[AnyRef]): LogoSeq = LogoSeq(seq)

}
