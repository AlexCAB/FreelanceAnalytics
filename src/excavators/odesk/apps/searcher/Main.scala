package excavators.odesk.apps.searcher

import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.ui.{ExcavatorUI, Browser}
import util.logging.{ToDBAndConsoleLogger, SimpleLogger}

/**
* oDesk job searcher
* Created by CAB on 21.09.14.
*/

object Main {
  //Create components
  val l = new ToDBAndConsoleLogger("searcher")
  val db = new ODeskExcavatorsDBProvider
  val b = new Browser(l)
  val w = new Worker(b,l,db)
  val ui = new ExcavatorUI("oDesk job searcher and excavators manager", b, w, l, loadAndSetParam, closing)
  val wc = new Watcher(b,w,ui)
  l.setConsole(ui)
  l.setDB(db)
  //Methods
  def main(a:Array[String]):Unit = {
    //Load arguments
    if(a.size < 4){
      println("Error: Not enough arguments.")
      System.exit(-1)}
    val dbAddress = a(0)
    val dbUser = a(1)
    val dbPassword = a(2)
    val dbName = a(3)
    //Connect to DB
    println("Connect to: " + dbAddress +  ", user: " + dbUser + ", password: " + dbPassword + ", DB: " + dbName)
    db.init(dbAddress, dbUser, dbPassword, dbName)
    //Load and set parameters
    loadAndSetParam()
    //Run
    ui.init()
    w.init()
    wc.init()}
  def loadAndSetParam():Unit = {
    //Reload and set parameters
    val ps = db.loadParameters()
    l.setParameters(ps)
    b.setParameters(ps)
    w.setParameters(ps)
    l.info("[Main.loadAndSetParam] Parameters loaded and set.")}
  def closing():Unit = {
    //Stop
    wc.halt()
    w.halt()
    ui.halt()
    db.halt()
    //Exit
    System.exit(0)}}


