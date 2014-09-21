package excavators.odesk.ui
import excavators.helpers.{TestLogger, TestWorker}
import javafx.concurrent.Worker

/**
 * Excavator UI test tool
 * Created by CAB on 21.09.14.
 */

object ExcavatorUITest {
  def main(a:Array[String]):Unit = {
    val b = new Browser
    val w = new TestWorker(b)
    val l = new TestLogger
    val e = new ExcavatorUI(b, w, l, ()=>{
      println("=== closing ===")
      System.exit(0)})
    e.init()}}

