package excavators.odesk.ui

/**
 * Interface for worker management
 * Created by CAB on 21.09.14.
 */
trait ManagedWorker {
  def goToMain()
  def saveHtml()
  def setWork(s:Boolean):Unit
  def isWork:Boolean}
