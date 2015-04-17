package org.levelspace

import java.awt.Dimension
import java.awt.event.{ItemEvent, TextEvent, TextListener, ActionEvent}
import javax.swing.SpringLayout._
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing._

import net.miginfocom.swing._
import org.nlogo.api.{CompilerException, LogoList, Observer, SimpleJobOwner}
import org.nlogo.app.EditorFactory
import org.nlogo.window.GUIWorkspace
import uk.ac.surrey.xw.api._
import uk.ac.surrey.xw.api.ExtraWidget
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

class ProcedureWidget(val key: WidgetKey, val state: State, implicit val ws: GUIWorkspace) extends LabeledPanelWidget {
  import org.levelspace.Enhancer._

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

  val saveButton: JButton = makeButton("save", tryCompilation(ws, owner, () => saveCommand))
  val deleteButton: JButton = makeButton("delete", tryCompilation(ws, owner, () => deleteCommand))
  val nameField: JTextField = boundTextField(10, kind.nameProperty)
  val argField: JTextField = boundTextField(0, kind.argProperty)

  removeAll()
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  add(buttonPanel)
  add(Box.createRigidArea(new Dimension(0, 3)))
  add(inputPanel)

  def buttonPanel: JPanel = {
    val panel = new JPanel()
    panel.setLayout(new MigLayout("insets 5"))
    panel.add(saveButton)
    panel.add(deleteButton)
    panel.setPreferredSize(panel.getMinimumSize())
    panel.setMaximumSize(panel.getMinimumSize())
    panel
  }

  def inputPanel: JPanel = {
    val panel = new JPanel()
    panel.setLayout(new MigLayout("fill, insets 0 5 5 5", "[][fill,grow]", "[][][][growprio 150]"))
    panel.add(new JLabel("Name:"), "shrink")
    panel.add(nameField, "growx, spanx, wrap")
    panel.add(new JLabel("Argument names:"), "shrink")
    panel.add(argField, "growx, spanx, wrap")
    panel.add(new JScrollPane(editor), "grow, spanx, spany, gapbottom 5")
    panel
  }

  def bindToProperty(field: JTextField, property: StringProperty[this.type]) =
    field.getDocument().addDocumentListener(new DocumentListener {
      override def changedUpdate(e: DocumentEvent): Unit = updateInState(property)
      override def removeUpdate(e: DocumentEvent): Unit = updateInState(property)
      override def insertUpdate(e: DocumentEvent): Unit = updateInState(property)
    })

  private def boundTextField(i: Int, prop: StringProperty[this.type]) = {
    val t = new JTextField(i)
    bindToProperty(t, prop)
    t
  }

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

  val runCommandProperty = new StringProperty[W]("RUN-COMMAND",
    Some(_.runCommand = _), _.runCommand)

  val selectedProcedureArguments = new ListProperty[W]("SELECTED-PROCEDURE-ARGUMENTS",
    Some((w, l) => w.selectedProcedureArguments = l), _.selectedProcedureArguments)

  val availableProcedureArguments = new ListProperty[W]("AVAILABLE-PROCEDURE-ARGUMENTS",
    Some((w,l) => w.availableProcedureArguments = l), _.availableProcedureArguments)

  val selectedAgentsetArguments = new ListProperty[W]("SELECTED-AGENTSET-ARGUMENTS",
    Some((w,l) => w.selectedAgentsetArguments = l), _.selectedAgentsetArguments)

  val availableAgentsetArguments = new ListProperty[W]("AVAILABLE-AGENTSET-ARGUMENTS",
    Some((w,l) => w.availableAgentsetArguments = l), _.availableAgentsetArguments)

  override val defaultProperty = None
  override def propertySet = super.propertySet ++ Set(
    selectedAgentReporterProperty,
    availableAgentReporterProperty,
    selectedProcedureProperty,
    availableProceduresProperty,
    saveCommandProperty,
    deleteCommandProperty,
    runCommandProperty,
    selectedProcedureArguments,
    availableProcedureArguments,
    selectedAgentsetArguments,
    availableAgentsetArguments
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
  var agentsetArguments  = Map.empty[String, XWComboBox]

  var saveCommand = ""
  var deleteCommand = ""
  var runCommand = ""

  removeAll()
  setLayout(new MigLayout("insets 5"))
  add(new JLabel("extended agentset"), "align right")
  val agentSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedAgentReporterProperty))
  add(agentSelector, "grow, wrap")

  val agentArgumentPanel = new JPanel()
  add(agentArgumentPanel, "grow, span, wrap")

  val agentsetArgumentPanel = new JPanel()
  add(agentsetArgumentPanel, "grow, span, wrap")

  add(new JLabel("commands"), "align right")
  val procedureSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedProcedureProperty))
  add(procedureSelector, "grow, wrap")

  val procedureArgumentPanel = new JPanel()
  add(procedureArgumentPanel, "grow, span, wrap")

  val buttonPanel = new JPanel()
  val saveButton = makeButton("save", tryCompilation(ws, owner, () => saveCommand))
  buttonPanel.add(saveButton)

  val deleteButton = makeButton("delete", tryCompilation(ws, owner, () => deleteCommand))
  buttonPanel.add(deleteButton)

  val runButton = makeButton("run", tryCompilation(ws, owner, () => runCommand))
  buttonPanel.add(runButton)

  add(buttonPanel, "grow, span")

  def selectedAgentsetArguments = selectedArguments(agentsetArguments)
  def selectedAgentsetArguments_=(args: LogoList): Unit = setSelectedArguments(agentsetArguments, args)

  def availableAgentsetArguments = availableArguments(agentsetArguments)
  def availableAgentsetArguments_=(args: LogoList) =
    agentsetArguments = setAvailableArguments(
      args, agentsetArgumentPanel, () => updateInState(kind.selectedAgentsetArguments))

  def selectedProcedureArguments = selectedArguments(procedureArguments)
  def selectedProcedureArguments_=(args: LogoList): Unit = setSelectedArguments(procedureArguments, args)

  def availableProcedureArguments = availableArguments(procedureArguments)
  def availableProcedureArguments_=(args: LogoList) =
    procedureArguments = setAvailableArguments(
      args, procedureArgumentPanel, () => updateInState(kind.selectedProcedureArguments))

  private def availableArguments(args: Map[String, XWComboBox]): LogoList =
    procedureArguments.map {
      case (name: String, comboBox: XWComboBox) => LogoList(name, comboBox.items.toLogo)
    }.toSeq.toLogo

  private def selectedArguments(args: Map[String, XWComboBox]): LogoList =
    args.map {
      case (name: String, comboBox: XWComboBox) => LogoList(name, comboBox.selectedItem)
    }.toSeq.toLogo

  private def setSelectedArguments(args: Map[String, XWComboBox], list: LogoList): Unit = list.foreach {
    case arg: LogoList =>
      val name = arg.get(0).asInstanceOf[String]
      val value = arg.get(1).asInstanceOf[String]
      args(name).selectedItem = value
    case x =>
      ws.warningMessage("Invalid selection item: " + x.toString)
  }

  private def setAvailableArguments(list: LogoList, panel: JPanel, update: () => Unit): Map[String, XWComboBox] = {
    val newArgs = assembleArguments(list, update)
    layoutArgumentPanel(panel, newArgs, update)
    newArgs
  }

  private def assembleArguments(argumentList: LogoList, update: () => Unit): Map[String, XWComboBox] = {
    val (names, items) = argumentList.map(_.asInstanceOf[LogoList]).map { l =>
      l.get(0).toString -> l.get(1).asInstanceOf[LogoList].map(_.toString)
    }.toSeq.unzip
    names.zip(items.map {
      opts =>
        val chooser: XWComboBox = new XWComboBox(() => update())
        chooser.items = opts.toSeq
        chooser
    }).toMap
  }

  private def layoutArgumentPanel[T <: ExtraWidget, S](panel: JPanel, arguments: Map[String, XWComboBox], update: () => Unit) = {
    panel.removeAll()
    panel.setLayout(new MigLayout())
    arguments.foreach {
      case (name: String, selector: XWComboBox) =>
        panel.add(new JLabel(name))
        panel.add(selector, "grow, wrap")
        selector.onItemStateChanged(event => if (event.getStateChange == ItemEvent.SELECTED) update())
        if (selector.getItemCount > 0) selector.setSelectedIndex(0)
        update()
    }
    panel.revalidate()
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

  def tryCompilation(ws: GUIWorkspace, owner: SimpleJobOwner, code: () => String)(e: ActionEvent) = {
    try ws.evaluateCommands(owner, code(), ws.world.observers, waitForCompletion = false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  }

  def makeButton[T](text: String, f: ActionEvent => T) = {
    val b = new JButton(text)
    b.onActionPerformed(f)
    b
  }
}
