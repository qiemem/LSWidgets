package org.levelspace

import javax.swing._

import java.awt.event.ItemEvent

import net.miginfocom.swing._

import uk.ac.surrey.xw.api.ExtraWidget

import org.nlogo.api.{CompilerException, LogoList, Observer, SimpleJobOwner}
import org.nlogo.window.GUIWorkspace

import uk.ac.surrey.xw.api.swing.enrichItemSelectable

class LSArgumentSelector(changeCallback: ()=>Unit, ws: GUIWorkspace, maxTextLength: Option[Int]) extends JPanel {
  import org.levelspace.Enhancer._
  private var arguments = Map.empty[String, XWComboBox]

  def selectedArguments: LogoList = selectedArgumentsLogoList(arguments)
  def selectedArguments_=(args: LogoList): Unit = setSelectedArguments(arguments, args)

  def selectedArgumentIndices: LogoList = arguments.map {
    case (name: String, box: XWComboBox) => LogoList(name, Double.box(box.getSelectedIndex))
  }.toSeq.toLogo

  def selectedArgumentIndices_=(argIndexPairs: LogoList): Unit = argIndexPairs.foreach {
    case arg: LogoList =>
      val name = arg.get(0).asInstanceOf[String]
      val index = arg.get(1).asInstanceOf[Double].toInt
      arguments(name).setSelectedIndex(index)
    case x => ws.warningMessage("Invalid selection item: " + x.toString)
  }

  def availableArguments: LogoList = availableArgumentsLogoList(arguments)
  def availableArguments_=(args: LogoList) = {
    arguments = assembleArguments(args)
    layoutArgumentPanel(arguments)
  }

  private def selectedArgumentsLogoList(args: Map[String, XWComboBox]): LogoList =
    args.map {
      case (name: String, comboBox: XWComboBox) => LogoList(name, comboBox.selectedItem)
    }.toSeq.toLogo

  private def availableArgumentsLogoList(args: Map[String, XWComboBox]): LogoList =
    args.map {
      case (name: String, comboBox: XWComboBox) => LogoList(name, comboBox.items.toLogo)
    }.toSeq.toLogo

  private def setSelectedArguments(args: Map[String, XWComboBox], list: LogoList): Unit = list.foreach {
    case arg: LogoList =>
      val name = arg.get(0).asInstanceOf[String]
      val value = arg.get(1).asInstanceOf[String]
      args(name).selectedItem = value
    case x => ws.warningMessage("Invalid selection item: " + x.toString)
  }

  private def assembleArguments(argumentList: LogoList): Map[String, XWComboBox] = {
    val (names, items) = argumentList.map(_.asInstanceOf[LogoList]).map { l =>
      l.get(0).toString -> l.get(1).asInstanceOf[LogoList].map(_.toString)
    }.toSeq.unzip
    names.zip(items.map {
      opts =>
        val chooser: XWComboBox = new XWComboBox(changeCallback, maxChars = maxTextLength)
        chooser.items = opts.toSeq
        chooser
    }).toMap
  }

  private def layoutArgumentPanel[T <: ExtraWidget, S](arguments: Map[String, XWComboBox]) = {
    removeAll()
    setLayout(new MigLayout("insets 0"))
    arguments.foreach {
      case (name: String, selector: XWComboBox) =>
        add(new JLabel(name))
        add(selector, "grow, wrap")
        selector.onItemStateChanged(event => if (event.getStateChange == ItemEvent.SELECTED) changeCallback())
        if (selector.getItemCount > 0) selector.setSelectedIndex(0)
        changeCallback()
    }
    revalidate()
  }
}
