package excavators.util.tasks
import scala.collection.mutable.{Set => MutSet}

/**
 * Execute task form task queue by priority
 * Created by CAB on 20.10.2014.
 */

trait TaskExecutor {
  //Parameters
  val checkTimeOut = 500L
  val endWorkTimeOut = 600000L
  //Variables
  private var work = false
  private val taskQueue = MutSet[Task]()
  //Methods
  def start() = executorThread.synchronized{
    work = true
    executorThread.start()}
  def stop() = {
    executorThread.synchronized{
      work = false
      executorThread.notify()}
    executorThread.join(endWorkTimeOut)}
  def addTask(t:Task) = {
    taskQueue.synchronized{taskQueue += t}
    executorThread.synchronized{executorThread.notify()}}
  def queueSize = taskQueue.size
  def isWork:Boolean = work
  //Thread
  private val executorThread:Thread = new Thread{override def run() = {
    while(work){
      //Search next task
      val nt = taskQueue.synchronized{
        if(taskQueue.nonEmpty){
          Some(taskQueue.maxBy(_.priority))}
        else{
          None}}
      //Execute task or wait
      nt match{
        case Some(t) => {
          taskQueue -= t
          try{
            t.execute()} //Execute task
          catch{
            case e:Exception => e.printStackTrace()
            case e:Error => e.printStackTrace()}}
        case None => {
          synchronized{wait(checkTimeOut)}}}}}}
  executorThread.setDaemon(true)}