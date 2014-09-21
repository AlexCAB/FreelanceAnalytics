package excavators.helpers
import excavators.odesk.ui.{Browser, ManagedWorker}

/**
 * Test ManagedWorker implementation
 * Created by WORK on 21.09.14.
 */

class TestWorker(b:Browser) extends ManagedWorker {
  private var w = false
  def goToMain() = {b.openURL("http://www.google.com")}
  def saveHtml() = {println(b.getCurrentHTML)}
  def setWork(s:Boolean):Unit = {println("TestWorker.setWork = " + s); w = s}
  def isWork:Boolean = w}
