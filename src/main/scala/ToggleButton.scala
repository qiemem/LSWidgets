package org.levelspace

import javax.swing.JToggleButton

import org.nlogo.window.GUIWorkspace
import org.nlogo.window.InterfaceColors._
import uk.ac.surrey.xw.api._
import uk.ac.surrey.xw.api.swing.enrichItemSelectable

class ToggleButtonKind[W <: ToggleButton] extends ComponentWidgetKind[W] {
  val newWidget = new ToggleButton(_, _, _)
  val name = "TOGGLE-BUTTON"

  val selectedProperty =
    new BooleanProperty[W]("SELECTED", Some(_.setSelected(_)), _.isSelected)
  override def propertySet = super.propertySet ++ Set(
    selectedProperty,
    new StringProperty[W]("LABEL", Some(_.setText(_)), _.getText))

  def defaultProperty = Some(selectedProperty)
}

// AbstractButtonWidget inherits from JComponentWidget, which sets coloring.
// I wanted to keep default coloring. - BCH 4/28/2015
class ToggleButton(val key: WidgetKey, val state: State, val ws: GUIWorkspace)
  extends JToggleButton with ComponentWidget {
  val kind = new ToggleButtonKind[this.type]

  this.onItemStateChanged{ _ =>
    updateInState(kind.selectedProperty)
  }
}
