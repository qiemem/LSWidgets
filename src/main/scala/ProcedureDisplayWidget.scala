package org.levelspace

import java.awt.Color
import java.awt.event.{ ActionEvent, MouseListener }

import javax.swing.{ AbstractAction, JTextField, JCheckBox, JLabel }

import net.miginfocom.swing._

import org.nlogo.window.GUIWorkspace

import uk.ac.surrey.xw.api._

class ProcedureDisplayWidgetKind[W <: ProcedureDisplayWidget] extends LabeledPanelWidgetKind[W] {
  val newWidget = new ProcedureDisplayWidget(_, _, _)
  val name = "PROCEDURE-DISPLAY-WIDGET"

  val nameProperty = new StringProperty[W]("NAME",
    Some((w, s) => { w.nameField.setText(s) }), _.nameField.getText)
  val argProperty = new StringProperty[W]("ARGS",
    Some((w, s) => { w.argField.setText(s) }), _.argField.getText)
  val typeProperty = new StringProperty[W]("TYPE",
    Some((w, s) => { w.typeField.setText(s) }), _.typeField.getText)
  val visibleProperty = new BooleanProperty[W]("VISIBLE",
    Some((w, s) => { w.visibleCheckBox.setSelected(s) }), _.visibleCheckBox.isSelected)
  val defaultProperty = Some(visibleProperty)
  override def propertySet = super.propertySet ++
    Set(nameProperty, argProperty, typeProperty, visibleProperty)
}

class ProcedureDisplayWidget(val key: WidgetKey, val state: State, val ws: GUIWorkspace) extends LabeledPanelWidget {
  import org.levelspace.Enhancer._

  override val kind = new ProcedureDisplayWidgetKind[this.type]

  val nameField: JTextField = new DisabledTextField()
  val argField: JTextField = new DisabledTextField()
  val typeField: JTextField = new DisabledTextField()
  val visibleCheckBox: JCheckBox = new JCheckBox()
  visibleCheckBox.addActionListener(new AbstractAction {
    def actionPerformed(e: ActionEvent) =
      updateInState(kind.visibleProperty)
  })

  removeAll()
  setLayout(new MigLayout("fill, insets 5", "[shrink][grow 105][][]", ""))
  add(new JLabel(I18N.get("agentset.name")), "shrink")
  add(nameField, "growx")
  add(new JLabel(I18N.get("agentset.visible")), "shrink")
  add(visibleCheckBox, "pad 7 0 7 0, wrap")
  add(new JLabel(I18N.get("agentset.argument_names")), "shrink")
  add(argField, "growx, spanx, wrap")
  add(new JLabel(I18N.get("agentset.type")), "shrink")
  add(typeField, "growx, spanx")
}

class DisabledTextField extends JTextField {
  override def addMouseListener(l: MouseListener) {}

  setBackground(new Color(215, 215, 215))
  setEnabled(false)
  setEditable(false)
}
