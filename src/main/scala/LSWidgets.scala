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
import org.nlogo.api.NumberParser
import org.nlogo.app.EditorFactory
import org.nlogo.awt.EventQueue.invokeLater
import org.nlogo.window.GUIWorkspace

import javax.swing.{JScrollPane, JTextArea, JEditorPane, JTextField}
import javax.swing.KeyStroke.getKeyStroke
import uk.ac.surrey.xw.api.DoubleProperty
import uk.ac.surrey.xw.api.LabeledPanelWidget
import uk.ac.surrey.xw.api.LabeledPanelWidgetKind
import uk.ac.surrey.xw.api.State
import uk.ac.surrey.xw.api.StringProperty
import uk.ac.surrey.xw.api.WidgetKey
import uk.ac.surrey.xw.api.swing.enrichComponent
import uk.ac.surrey.xw.api.swing.newAction
import uk.ac.surrey.xw.api.toRunnable

class ProcedureWidgetKind[W <: ProcedureWidget] extends LabeledPanelWidgetKind[W] {
  val newWidget = new ProcedureWidget(_, _, _)
  val name = "PROCEDURE-WIDGET"
  val codeProperty = new StringProperty[W]("CODE",
    Some((w, s) ⇒ { w.code = s; w.editor.setText(s) }),
    _.code)
  val nameProperty = new StringProperty[W]("NAME",
    Some((w, s) => { w.nameField.setText(s) }),
    _.nameField.getText)
  val argProperty = new StringProperty[W]("ARGS",
    Some((w, s) => { w.argField.setText(s) }),
    _.argField.getText)
  val defaultProperty = Some(codeProperty)
  override def propertySet = super.propertySet ++ Set(codeProperty, nameProperty, argProperty)
}

class ProcedureWidget(
  val key: WidgetKey,
  val state: State,
  val ws: GUIWorkspace)
  extends LabeledPanelWidget {

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
  add(nameField)

  val argLabel = new JLabel("Argument names:")
  springLayout.putConstraint(WEST, argLabel, bigSpace, EAST, nameField)
  springLayout.putConstraint(NORTH, argLabel, bigSpace, NORTH, this)
  add(argLabel)

  val argField = new JTextField()
  bindToProperty(argField, kind.argProperty)
  springLayout.putConstraint(WEST, argField, smallSpace, EAST, argLabel)
  springLayout.putConstraint(NORTH, argField, smallSpace, NORTH, this)
  springLayout.putConstraint(EAST, argField, -bigSpace, EAST, this)
  add(argField)

  val scrollPane = new JScrollPane(editor)
  springLayout.putConstraint(NORTH, scrollPane, bigSpace, SOUTH, nameLabel)
  springLayout.putConstraint(NORTH, scrollPane, bigSpace, SOUTH, nameField)
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
