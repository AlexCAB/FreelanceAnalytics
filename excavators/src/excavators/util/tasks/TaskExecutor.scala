package excavators.util.tasks
import scala.collection.mutable.Queue

/**
 * Execute task form task queue
 * Created by WORK on 13.10.2014.
 */

trait TaskExecutor{
  //Variables
  private var work = false
  private var paused = false
  private val taskQueue = Queue[Task]()
  //Functions

  //Methods
  def start() = executorThread.synchronized{
    work = true
    executorThread.start()}
  def stop() = {
    executorThread.synchronized{
      work = false
      notify()}
    executorThread.join(600000)}
  def setPaused(f:Boolean) = {
    if(f){executorThread.synchronized{notify()}}
    paused = f}
  def addTask(t:Task) = {

  }
  //Thread
  private val executorThread = new Thread{override def run() = {







  }}
  executorThread.setDaemon(true)}
