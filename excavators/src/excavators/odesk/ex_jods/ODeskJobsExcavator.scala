package excavators.odesk.ex_jods

import excavators.odesk.db.DBProvider
import excavators.odesk.ui.{ExcavatorUI, Browser}
import excavators.util.logging.{DBConsoleLogger, SimpleLogger}

/**
 * oDesk job excavator - collect new jobs from search
 * Created by CAB on 21.09.14.
 */

object ODeskJobsExcavator {
  //Create components
  val l = new DBConsoleLogger("ODeskJobsExcavator")
  val db = new DBProvider
  val s = new Saver(l,db)
  val b = new Browser
  val w = new Worker(b,l,s,db)
  val ui = new ExcavatorUI(b, w, l, closing)
  l.setConsole(ui)
  l.setDB(db)
  //Methods
  def main(a:Array[String]):Unit = {
    //Run
    db.init("jdbc:mysql://127.0.0.1:3306", "root", "qwerty", "freelance_analytics")
    s.start()
    ui.init()
    w.init()}
  def closing():Unit = {
    //Stop
    w.halt()
    ui.halt()
    s.stop()
    db.halt()
    //Exit
    System.exit(0)}}


