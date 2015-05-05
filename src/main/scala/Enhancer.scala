package org.levelspace

import java.awt.event.ActionEvent

import javax.swing.JButton

import org.nlogo.window.GUIWorkspace
import org.nlogo.api.{CompilerException, LogoList, SimpleJobOwner}

import uk.ac.surrey.xw.api.swing.{enrichItemSelectable, enrichAbstractButton}

object Enhancer {
  case class LogoSeq(seq: Seq[AnyRef]) {
    def toLogo: LogoList = LogoList.fromIterator(seq.iterator)
  }

  implicit def toLogoSeq(seq: Seq[AnyRef]): LogoSeq = LogoSeq(seq)

  def tryCompilation(ws: GUIWorkspace, owner: SimpleJobOwner, code: () => String)(e: ActionEvent) = {
    try ws.evaluateCommands(owner, code(), ws.world.observers, waitForCompletion = false)
    catch { case e: CompilerException => ws.warningMessage(e.getMessage) }
  }

  def makeButton[T](key: String, f: ActionEvent => T) = {
    val b = new JButton(I18N.get(key))
    b.onActionPerformed(f)
    b
  }
}
