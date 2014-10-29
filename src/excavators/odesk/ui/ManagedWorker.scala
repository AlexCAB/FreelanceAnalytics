package excavators.odesk.ui

/**
 * Interface for worker management
 * Created by CAB on 21.09.14.
 */
trait ManagedWorker {
  def goToMain()
  def saveHtml()
  def saveScreenshot()
  def setPaused(s:Boolean):Unit
  def isPaused:Boolean}
