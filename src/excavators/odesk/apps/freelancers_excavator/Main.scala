package excavators.odesk.apps.freelancers_excavator

import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.ui.{ExcavatorUI, Browser}
import util.logging.{ToDBAndConsoleLogger, SimpleLogger}

/**
* oDesk freelancer excavator
* Created by CAB on 19.11.2014.
*/

object Main {
  //Variables
  private var en = -1
  //Create components
  val l = new ToDBAndConsoleLogger("freelancers_excavator")
  val db = new ODeskExcavatorsDBProvider
  val b = new Browser(l)
  val s = new Saver(l,db)
  val w = new Worker(b,l,s,db)
  val ui = new ExcavatorUI("Freelancers excavator ", b, w, l, loadAndSetParam, closing)
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
    //Registration of self
    en = db.registrateFreelancersExcavator
    //Run
    s.start()
    ui.init()
    w.init(en)
    wc.init()
    ui.title = "Freelancers excavator №" + en}
  def loadAndSetParam():Unit = {
    //Reload and set parameters
    val ps = db.loadParameters()
    l.setParameters(ps)
    b.setParameters(ps)
    w.setParameters(ps)
    s.setParameters(ps)
    l.info("[Main.loadAndSetParam] Parameters loaded and set.")}
  def closing():Unit = {
    //Unregistration of self
    db.unregistrateFreelancersExcavator(en)
    //Stop
    wc.halt()
    w.halt()
    ui.halt()
    s.stop()
    db.halt()
    //Exit
    System.exit(0)}}


