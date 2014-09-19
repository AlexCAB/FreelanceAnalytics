package excavators.odesk

import scala.collection.mutable.Queue

object Worker extends Thread{
  //Parameters
  val checkTimeout = 1000 //In seconds
  val mainPageURL = "https://www.odesk.com"
  val jobSearchURL = "https://www.odesk.com/jobs/?q="
  val desktopFolder = System.getProperty("user.home") + "/Desktop"
  //Components
  val htmlParser = new HTMLParsers
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
  def goToMain() = {
    if(! work){
      Browser.openURL(mainPageURL)}
    else{
      Logger.log("[Worker.goToMain] Can't go to main page when work.")}}
  def saveHtml() = {
    Browser.getCurrentHTML match{
      case Some(t) => {
        val p = desktopFolder + "\\" + System.currentTimeMillis() + ".html"
        try{
          tools.nsc.io.File(p).writeAll(t)
          Logger.log("[Worker.saveHtml] File save to: " + p)}
        catch{case e:Exception => {
          Logger.log("[Worker.saveHtml] Exception when save: " + e)}}}
      case None => Logger.log("[Worker.saveHtml] Not save, empty.")}}
  def setWork(s:Boolean) = {
    work = s
    if(s){
      synchronized{notify()}
      Logger.log("[Worker.setWork] Run.")}
    else{
      Logger.log("[Worker.setWork] Paused.")}}
  //Run method
  override def run():Unit = {
    synchronized{wait()}
    while(live){
      //Search new works
//      val lw = Browser.getHTMLbyURL(jobSearchURL) match {
//        case Some(html) => htmlParser.parseWorkSearchResult(html)   //!!! Если парсер венул пустой список, поторобувать перезагрузить страницу( всего 3 попытки)
//        case None => {                                               //Повыносить это всё в отдельные функции.
//          Logger.log("[Worker.run] Error: Can't get search works html")           //Приоретитеты задачь.
//          List()}}
//
//      //
//      println(Browser.getHTMLbyURL(mainPageURL + lw(0).url))
//
//      //Add new works to db
//      lw.foreach(w => {
//        println(w.url)
//        println(w.skills)
//
//      })

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
