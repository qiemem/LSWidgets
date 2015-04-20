package org.levelspace

import java.awt.Dimension
import java.awt.event.{ TextEvent, TextListener }
import javax.swing.SpringLayout._
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing._

import net.miginfocom.swing._
import org.nlogo.api.{ Observer, SimpleJobOwner }
import org.nlogo.app.EditorFactory
import org.nlogo.window.GUIWorkspace
import uk.ac.surrey.xw.api._
import uk.ac.surrey.xw.api.ExtraWidget

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

  val saveButton: JButton = makeButton(
    "agentset.save", tryCompilation(ws, owner, () => saveCommand))
  val deleteButton: JButton = makeButton(
    "agentset.delete", tryCompilation(ws, owner, () => deleteCommand))
  val nameField: JTextField = boundTextField(kind.nameProperty)
  val argField: JTextField = boundTextField(kind.argProperty)

  val buttonPanel: JPanel = {
    val panel = new JPanel()
    panel.setLayout(new MigLayout("insets 5"))
    panel.add(saveButton)
    panel.add(deleteButton)
    panel.setPreferredSize(panel.getMinimumSize())
    panel.setMaximumSize(panel.getMinimumSize())
    panel
  }

  val inputPanel: JPanel = {
    val panel = new JPanel()
    panel.setLayout(new MigLayout("fill, insets 0 5 5 5", "[][fill,grow]", "[][][][growprio 150]"))
    panel.add(new JLabel(I18N.get("agentset.name")), "shrink")
    panel.add(nameField, "growx, spanx, wrap")
    panel.add(new JLabel(I18N.get("agentset.argument_names")), "shrink")
    panel.add(argField, "growx, spanx, wrap")
    panel.add(new JScrollPane(editor), "grow, spanx, spany, gapbottom 5")
    panel
  }

  removeAll()
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  add(buttonPanel)
  add(Box.createRigidArea(new Dimension(0, 3)))
  add(inputPanel)

  def bindToProperty(field: JTextField, property: StringProperty[this.type]) =
    field.getDocument().addDocumentListener(new DocumentListener {
      override def changedUpdate(e: DocumentEvent): Unit = updateInState(property)
      override def removeUpdate(e: DocumentEvent): Unit = updateInState(property)
      override def insertUpdate(e: DocumentEvent): Unit = updateInState(property)
    })

  private def boundTextField(prop: StringProperty[this.type]) = {
    val t = new JTextField()
    bindToProperty(t, prop)
    t
  }

}
