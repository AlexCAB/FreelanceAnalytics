package excavators.odesk.ex_jods

import scala.collection.mutable.Queue
import excavators.odesk.parsers.HTMLParsers
import excavators.odesk.ui.{ManagedWorker, Browser}
import excavators.odesk.logging.Logger



class Worker(browser:Browser, logger:Logger) extends Thread with ManagedWorker{
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
  private var live = true
  private var work = false
  //Methods
  def init() = {
    start()}
  def halt() = {
    live = false; synchronized{notify()}
    join(600000)}
  def goToMain() = {
    if(! work){
      browser.openURL(mainPageURL)}
    else{
      logger.log("[Worker.goToMain] Can't go to main page when work.")}}
  def saveHtml() = {
    browser.getCurrentHTML match{
      case Some(t) => {
        val p = desktopFolder + "\\" + System.currentTimeMillis() + ".html"
        try{
          tools.nsc.io.File(p).writeAll(t)
          logger.log("[Worker.saveHtml] File save to: " + p)}
        catch{case e:Exception => {
          logger.log("[Worker.saveHtml] Exception when save: " + e)}}}
      case None => logger.log("[Worker.saveHtml] Not save, empty.")}}
  def saveScreenshot() = {}
  def setWork(s:Boolean) = {
    work = s
    if(s){
      synchronized{notify()}
      logger.log("[Worker.setWork] Run.")}
    else{
      logger.log("[Worker.setWork] Paused.")}}
  def isWork:Boolean = work
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
