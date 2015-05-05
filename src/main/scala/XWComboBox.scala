package org.levelspace

import java.awt.event.ItemEvent

import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.JComboBox

import uk.ac.surrey.xw.api.swing.{enrichItemSelectable, enrichAbstractButton}

class XWComboBox(selectionCallback: ()=>Unit, editable: Boolean=true) extends JComboBox {
  setEditable(editable)

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
