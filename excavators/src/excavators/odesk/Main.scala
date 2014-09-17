package excavators.odesk

object Main {
  def main(a:Array[String]):Unit = {
    //Initialization components
    UI.init()
    Worker.init()
    //End initialization
    Logger.log("oDesk excavator ready. Browser: " + Browser.getBrowserInfo())}
  def termination() = {
    //Stop components
    UI.halt()
    Worker.halt()
    //Exit
    System.exit(0)}}
