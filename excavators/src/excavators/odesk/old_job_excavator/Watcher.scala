package excavators.odesk.old_job_excavator

import excavators.odesk.db.Saver
import excavators.odesk.ui.{ExcavatorUI, Browser}

/**
* Component watch work process and print metrics to status bar.
* Created by CAB on 23.10.2014.
*/

class Watcher(browser:Browser, worker:Worker, saver:Saver, ui:ExcavatorUI) {
  //Parameters
  val updateTimeOut = 1000
  val stopTimeout = 2000
  //Variables
  private var work = false
  //Worker thread
  private val workerThread = new Thread{override def run():Unit = {while(work){
    //Get data
    val tl = browser.getMetrics
    val (wqs,npj,pq,npf) = worker.getMetrics
    val (sqs,ts) = saver.getMetrics
    //Build report
    val r = List(
      ("Worker q. size",wqs.toString),
      ("Saver q. size",sqs.toString),
      ("N proc job",npj.toString),
      ("Parsing q.",pq.toString),
      ("N parsing fails", npf.toString),
      ("T load",(tl.toDouble / 1000).toString),
      ("T save",(ts.toDouble / 1000).toString))
    //Print report
    ui.printStatus(r)
    //Wait next update
    synchronized{wait(updateTimeOut)}}}}
  workerThread.setDaemon(true)
  //Methods
  def init() = {
    work = true
    workerThread.start()}
  def halt() = {
    work = false
    workerThread.synchronized{workerThread.notify()}
    workerThread.join(stopTimeout)
    ui.printStatus(List(("Program stopping work...","")))}}
