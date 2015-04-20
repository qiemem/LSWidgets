package org.levelspace

import javax.swing.{ JPanel, JLabel }

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

  removeAll()
  setLayout(new MigLayout("insets 5", "", "[][shrink 105][][shrink 105][]"))
  add(new JLabel(I18N.get("relationship.agentset")), "align right")
  val agentSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedAgentReporterProperty))
  add(agentSelector, "growx, wrap")

  val agentsetArgumentPanel: LSArgumentSelector =
    new LSArgumentSelector(() => updateInState(kind.selectedAgentsetArguments), ws)
  add(agentsetArgumentPanel, "gapleft 0:10:20, spanx, wrap")

  add(new JLabel(I18N.get("relationship.commands")), "align right")
  val procedureSelector: XWComboBox = new XWComboBox(() => updateInState(kind.selectedProcedureProperty))
  add(procedureSelector, "growx, wrap")

  val procedureArgumentPanel: LSArgumentSelector =
    new LSArgumentSelector(() => updateInState(kind.selectedProcedureArguments), ws)
  add(procedureArgumentPanel, "gapleft 0:10:20, spanx, wrap")

  val buttonPanel = new JPanel()
  val saveButton = makeButton("relationship.save", tryCompilation(ws, owner, () => saveCommand))
  buttonPanel.add(saveButton)

  val deleteButton = makeButton("relationship.delete", tryCompilation(ws, owner, () => deleteCommand))
  buttonPanel.add(deleteButton)

  val runButton = makeButton("relationship.run", tryCompilation(ws, owner, () => runCommand))
  buttonPanel.add(runButton)

  add(buttonPanel, "growx, spanx")
}

