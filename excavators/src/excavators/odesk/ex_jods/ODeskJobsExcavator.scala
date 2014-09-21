package excavators.odesk.ex_jods

import excavators.odesk.ui.{ExcavatorUI, Browser}
import excavators.odesk.logging.SimpleLogger

/**
 * oDesk job excavator - collect new jobs from search
 * Created by CAB on 21.09.14.
 */

object ODeskJobsExcavator {
  //Create components
  val b = new Browser
  val l = new SimpleLogger
  val w = new Worker(b,l)
  val ui = new ExcavatorUI(b, w, l, closing)
  //Methods
  def main(a:Array[String]):Unit = {
    //Run
    ui.init()
    w.init()}
  def closing():Unit = {
    //Stop
    w.halt()
    ui.halt()
    //Exit
    System.exit(0)}}


