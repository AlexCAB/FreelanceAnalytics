package excavators.odesk

import scala.collection.mutable.Queue

object Worker extends Thread{
  //Parameters
  val checkTimeout = 1000 //In seconds
  val jobSearchURL = "https://www.odesk.com/jobs/?q="
  //Variables
  private val urlQueue = Queue[String]()
  //Fields
  var live = true
  var work = false
  //Methods
  def init() = {
    start()}
  def halt() = {
    live = false; synchronized{notify()}
    join(600000)}
  def setWork(s:Boolean) = {
    work = s
    if(s){
      synchronized{notify()}
      Logger.log("Worker run.")}
    else{
      Logger.log("Worker paused.")}}
  //Run method
  override def run():Unit = {
    synchronized{wait()}
    while(live){
      //Search new works
      val lw = Browser.getHTMLbyURL(jobSearchURL) match {
        case Some(html) => HTMLParsers.parseWorkSearchResult(html)   //!!! Если парсер венул пустой список, поторобувать перезагрузить страницу( всего 3 попытки)
        case None => {                                               //Повыносить это всё в отдельные функции.
          Logger.log("Error: Can't get search works html")
          List()}}
      //Add new works to db
      lw.foreach(w => {
        println(w.url)
        println(w.skills)

      })

      //Build check URL queue



      //Process URL
      while(live && urlQueue.nonEmpty){
        val nu = urlQueue.dequeue()




      }



      //Timeout
      if(work){
        synchronized{wait(checkTimeout * 1000)}} //Timeout to next check
      else{
        synchronized{wait()}}}} //Paused







}
