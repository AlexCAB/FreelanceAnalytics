package excavators.odesk.apps.searcher

import excavators.odesk.ui.{ExcavatorUI, Browser}

/**
* Component watch work process and print metrics to status bar.
* Created by CAB on 23.10.2014.
*/

class Watcher(browser:Browser, worker:Worker, ui:ExcavatorUI) {
  //Parameters
  val updateTimeOut = 1000
  val stopTimeout = 2000
  //Variables
  private var work = false
  //Worker thread
  private val workerThread = new Thread{override def run():Unit = {while(work){
    //Get data
    val tl = browser.getMetrics
    val (wqs,nfj) = worker.getMetrics
    //Build report
    val r = List(
      ("Worker q. size",wqs.toString),
      ("N found job",nfj.toString),
      ("T load",(tl.toDouble / 1000).toString))
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
