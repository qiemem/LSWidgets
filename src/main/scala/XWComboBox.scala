package org.levelspace

import java.awt.Component
import java.awt.event.ItemEvent

import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.{ JComboBox, JList }
import javax.swing.plaf.basic.BasicComboBoxRenderer

import uk.ac.surrey.xw.api.swing.{enrichItemSelectable, enrichAbstractButton}

class XWComboBox(selectionCallback: ()=>Unit, editable: Boolean=true, maxChars: Option[Int] = None) extends JComboBox {
  setEditable(editable)

  def selectedItem: String = Option(getSelectedItem).map(_.toString).getOrElse("")

  def selectedItem_=(item: String) = setSelectedItem(item)

  def items: Seq[String] = (0 until getItemCount).map(getItemAt(_).toString)

  def items_=(items: Seq[AnyRef]) = {
    removeAllItems()
    items.foreach(addItem)
    setSelectedItem(items.headOption.orNull)
  }

  maxChars.foreach(charLimit => setRenderer(new XWComboBoxRenderer(charLimit)))

  this.onItemStateChanged { event =>
    if (event.getStateChange == ItemEvent.SELECTED) selectionCallback()
  }

  class XWComboBoxRenderer(maxChars: Int) extends BasicComboBoxRenderer {
    override def getListCellRendererComponent(list:         JList,
                                              value:        AnyRef,
                                              index:        Int,
                                              isSelected:   Boolean,
                                              cellHasFocus: Boolean): Component = {
      value match {
        case str: String if str.length > maxChars =>
          super.getListCellRendererComponent(list, str.take(maxChars - 3) + "...", index, isSelected, cellHasFocus)
        case _                                    =>
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
      }
    }
  }
}
