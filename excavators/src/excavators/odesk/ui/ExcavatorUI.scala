package excavators.odesk.ui

import scala.collection.mutable.{Map => MutMap, Set => MutSet, ListBuffer => MutList}
import chrriis.dj.nativeswing.swtimpl.NativeInterface
import scala.swing._
import scala.swing.event._
import excavators.odesk.logging.Logger
import javax.swing.SwingUtilities

/**
 * Excavator UI
 * Created by CAB on 21.09.14.
 */

class ExcavatorUI(browser:Browser, worker:ManagedWorker, logger:Logger, closing:()=>Unit) extends Frame with Console{
  //Parameters
  val maxLogSize = 100
  //Variables
  private var ready = false
  val logList = MutList[String]()
  //Construction
  title = "oDesk excavator"
  preferredSize = new Dimension(1000, 600)
  override def closeOperation():Unit = {closing()}
  //Init thread
  private val niThread = new Thread{
    override def run():Unit = {
      NativeInterface.open()
      NativeInterface.runEventPump()}}
  //Components
  private val loggerPnl = new TextArea(){editable = false}
  //Functions
  val printRunnable = new Runnable{
    def run() = logList.synchronized{
        if(logList.size > maxLogSize){logList.remove(0)}
        loggerPnl.text = logList.mkString("\n")}}
  //UI
  private val mainPanel = new BorderPanel {
    import BorderPanel.Position._
    import FlowPanel.Alignment._
    layout(new FlowPanel(Left)(){
      contents += new Button("To main"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          worker.goToMain()}}
      contents += new Button("Get HTML"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          worker.saveHtml()}}
      contents += new Button("RUN"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          worker.setWork(! worker.isWork)
          text = if(worker.isWork){"STOP"}else{"RUN"}}}}) = North
    layout(new ScrollPane(loggerPnl){preferredSize = new Dimension(0,80)}) = South}
  contents = mainPanel
  //Methods
  def init() = {
    //Run niThread
    niThread.start()
    while(niThread.isAlive && (! NativeInterface.isOpen)){Thread.sleep(50)}
    //Set browser
    import java.awt.BorderLayout
    mainPanel.peer.add(browser, BorderLayout.CENTER)
    logger.log("Browser: "  + browser.getBrowserInfo())
    //Show
    visible = true
    ready = true}
  def halt() = {
    NativeInterface.close()
    peer.dispose()}
  def print(s:String) = {
    logList.synchronized{logList += s}
    SwingUtilities.invokeLater(printRunnable)}
  def printLines(ls:List[String]) = {
    logList.synchronized{logList ++= ls}
    SwingUtilities.invokeLater(printRunnable)}}



