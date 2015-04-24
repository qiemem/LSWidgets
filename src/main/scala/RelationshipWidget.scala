package org.levelspace

import java.awt.Dimension

import javax.swing.{ JComponent, JSeparator, JPanel, JLabel, BoxLayout, SwingConstants }

import net.miginfocom.swing._

import org.nlogo.api.{LogoList, Observer, SimpleJobOwner}
import org.nlogo.window.GUIWorkspace

import uk.ac.surrey.xw.api._
import uk.ac.surrey.xw.api.swing.{enrichItemSelectable, enrichJButton}

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

  val upCommandProperty = new StringProperty[W]("UP-COMMAND",
    Some(_.upCommand = _), _.upCommand)

  val downCommandProperty = new StringProperty[W]("DOWN-COMMAND",
    Some(_.downCommand = _), _.downCommand)

  val selectedProcedureArguments = new ListProperty[W]("SELECTED-PROCEDURE-ARGUMENTS",
    Some((w, l) => w.procedureArgumentPanel.selectedArguments = l), _.procedureArgumentPanel.selectedArguments)

  val availableProcedureArguments = new ListProperty[W]("AVAILABLE-PROCEDURE-ARGUMENTS",
    Some((w,l) => w.procedureArgumentPanel.availableArguments = l), _.procedureArgumentPanel.availableArguments)

  val selectedAgentsetArguments = new ListProperty[W]("SELECTED-AGENTSET-ARGUMENTS",
    Some((w,l) => w.agentsetArgumentPanel.selectedArguments = l), _.agentsetArgumentPanel.selectedArguments)

  val availableAgentsetArguments = new ListProperty[W]("AVAILABLE-AGENTSET-ARGUMENTS",
    Some((w,l) => w.agentsetArgumentPanel.availableArguments = l), _.agentsetArgumentPanel.availableArguments)

  override val defaultProperty = None
  override def propertySet = super.propertySet ++ Set(
    selectedAgentReporterProperty,
    availableAgentReporterProperty,
    selectedProcedureProperty,
    availableProceduresProperty,
    saveCommandProperty,
    deleteCommandProperty,
    runCommandProperty,
    upCommandProperty,
    downCommandProperty,
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

  var saveCommand = ""
  var deleteCommand = ""
  var runCommand = ""
  var upCommand = ""
  var downCommand = ""

  val agentSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedAgentReporterProperty))
  val agentsetArgumentPanel: LSArgumentSelector =
    new LSArgumentSelector(() => updateInState(kind.selectedAgentsetArguments), ws)

  val procedureSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedProcedureProperty))
  val procedureArgumentPanel: LSArgumentSelector =
    new LSArgumentSelector(() => updateInState(kind.selectedProcedureArguments), ws)

  val saveButton = makeButton("relationship.save", tryCompilation(ws, owner, () => saveCommand))
  val deleteButton = makeButton("relationship.delete", tryCompilation(ws, owner, () => deleteCommand))
  val runButton = makeButton("relationship.run", tryCompilation(ws, owner, () => runCommand))
  val upButton = makeButton("relationship.up", tryCompilation(ws, owner, () => upCommand))
  val downButton = makeButton("relationship.down", tryCompilation(ws, owner, () => downCommand))

  val buttonPanel = {
    val p = new JPanel()
    p.add(saveButton)
    p.add(deleteButton)
    p.add(runButton)
    p
  }

  val contentPanel = {
    val p = new JPanel()
    p.setLayout(new MigLayout("insets 5 5 5 3", "", "[][shrink 105][][shrink 105][]"))
    p.add(new JLabel(I18N.get("relationship.agentset")), "align right")
    p.add(agentSelector, "growx, wrap")
    p.add(agentsetArgumentPanel, "gapleft 0:10:20, spanx, wrap")
    p.add(new JLabel(I18N.get("relationship.commands")), "align right")
    p.add(procedureSelector, "growx, wrap")
    p.add(procedureArgumentPanel, "gapleft 0:10:20, spanx, wrap")
    p.add(buttonPanel, "growx, spanx")
    p
  }

  val upDownButtonPanel = {
    val p = new JPanel()
    p.setLayout(new MigLayout("insets 5 3 5 5", "[shrink]", "[][grow][]"))
    shrinkX(upButton, 35)
    p.add(upButton, "wrap")
    p.add(new JPanel(), "growy, wrap")
    shrinkX(downButton, 35)
    p.add(downButton, "")
    shrinkX(p, p.getPreferredSize.width)
    p
  }

  def shrinkX(c: JComponent, width: Int): Unit = {
    c.setPreferredSize(new Dimension(width, c.getPreferredSize.height))
    c.setMaximumSize(new Dimension(width, c.getMaximumSize.height))
    c.revalidate()
  }

  removeAll()
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  add(contentPanel)
  add(new JSeparator(SwingConstants.VERTICAL))
  add(upDownButtonPanel)
}

