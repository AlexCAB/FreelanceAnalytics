package excavators.util.tasks
import scala.collection.mutable.{Set => MutSet}

/**
 * Execute task form task queue
 * Created by WORK on 13.10.2014.
 */

trait TaskExecutor{
  //Parameters
  val checkTimeOut = 1000L
  val endWorkTimeOut = 600000L
  //Variables
  private var work = false
  private var paused = false
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
  def setPaused(f:Boolean) = {
    if(! f){executorThread.synchronized{executorThread.notify()}}
    paused = f}
  def addTask(t:Task) = {
    taskQueue.synchronized{taskQueue += t}
    executorThread.synchronized{executorThread.notify()}}
  //Thread
  private val executorThread = new Thread{override def run() = {
    while(work){
      if(! paused){
        //Search next ready task and run
        val (nt,tm) = taskQueue.synchronized{
          if(taskQueue.nonEmpty){
            val ct = System.currentTimeMillis()
            val mt = taskQueue.map(_.time - ct).min
            if(mt <= 0){
              (Some(taskQueue.filter(_.time <= ct).maxBy(_.priority)), 0L)} //Get next task
            else{
              (None, mt)}} //No task at current time
          else{
            (None, checkTimeOut)}} //No task in queue then waite
        //Execute task or wait
        nt match{
          case Some(t) => {
            taskQueue -= t
            t.execute()}             //Execute task
          case None => {
            synchronized{wait(tm)}}}} //If no ready task then wait
      else{
        //If paused then white
        synchronized{wait(checkTimeOut)}}}}}
  executorThread.setDaemon(true)}
