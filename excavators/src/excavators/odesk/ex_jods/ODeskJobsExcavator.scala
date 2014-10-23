package excavators.odesk.ex_jods

import excavators.odesk.db.DBProvider
import excavators.odesk.ui.{ExcavatorUI, Browser}
import excavators.util.logging.{ToDBAndConsoleLogger, SimpleLogger}

/**
 * oDesk job excavator - collect new jobs from search
 * Created by CAB on 21.09.14.
 */

object ODeskJobsExcavator {
  //Create components
  val l = new ToDBAndConsoleLogger("ODeskJobsExcavator")
  val db = new DBProvider("odesk_new_job_excavator_param")
  val b = new Browser(l)
  val s = new Saver(l,db)
  val w = new Worker(b,l,s,db)
  val ui = new ExcavatorUI(b, w, l, loadAndSetParam, closing)
  val wc = new Watcher(b,w,s,ui)
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
    s.start()
    ui.init()
    w.init()
    wc.init()}
  def loadAndSetParam():Unit = {
    //Reload and set parameters
    val ps = db.loadParameters()
    l.setParameters(ps)
    b.setParameters(ps)
    w.setParameters(ps)
    l.info("[ODeskJobsExcavator.loadAndSetParam] Parameters loaded and set.")}
  def closing():Unit = {
    //Stop
    wc.halt()
    w.halt()
    ui.halt()
    s.stop()
    db.halt()
    //Exit
    System.exit(0)}}


