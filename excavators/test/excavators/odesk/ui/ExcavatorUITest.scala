package excavators.odesk.ui
import excavators.helpers.{TestLogger, TestWorker}
import javafx.concurrent.Worker

/**
 * Excavator UI (+Browser) test tool
 * Created by CAB on 21.09.14.
 */

object ExcavatorUITest {
  def main(a:Array[String]):Unit = {
    val l = new TestLogger
    val b = new Browser(l)
    val w = new TestWorker(b)
    val e = new ExcavatorUI(b, w, l,
      ()=>{
        println("=== reload parameters ===")},
      ()=>{
        println("=== closing ===")
        System.exit(0)})
    e.init()
    Thread.sleep(500)
    e.printStatus(List(("A","123"),("B","123456789"),("CCCCCCCCCCCC","221"),("D","1")))}}

