package excavators.odesk.ui

import java.awt.Font

import scala.collection.mutable.{Map => MutMap, Set => MutSet, ListBuffer => MutList}
import chrriis.dj.nativeswing.swtimpl.NativeInterface
import scala.swing._
import scala.swing.event._
import util.logging.{LoggerConsole, Logger}
import javax.swing.SwingUtilities
import BorderPanel.Position._
import FlowPanel.Alignment._
import java.awt.Color

/**
 * Excavator UI
 * Created by CAB on 21.09.14.
 */

class ExcavatorUI(name:String, browser:Browser, worker:ManagedWorker, logger:Logger, reloadParam:()=>Unit, closing:()=>Unit) extends Frame with LoggerConsole{
  //Parameters
  val maxLogSize = 100
  //Variables
  private var ready = false
  val logList = MutList[String]()
  //Construction
  title = name
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
  private val statBar = new TextArea(){
    text = "Status..."
    preferredSize = new Dimension(0,37)
    font = new Font(Font.MONOSPACED, Font.PLAIN, 12)
    border = Swing.MatteBorder(1,1,2,2,Color.gray)}
  private val mainPanel = new BorderPanel {
    layout(new FlowPanel(Left)(){
      contents += new Button("To main"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          worker.goToMain()}}
      contents += new Button("Get HTML"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          worker.saveHtml()}}
      contents += new Button("Screenshot"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          worker.saveScreenshot()}}
      contents += new Button("Up param"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          reloadParam()}}
      def wst(s:Boolean):String = if(s){"RUN"}else{"STOP"}
      contents += new Button(wst(worker.isPaused)){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          worker.setPaused(! worker.isPaused)
          text = wst(worker.isPaused)}}}) = North
    layout(new BorderPanel {
      layout(new ScrollPane(loggerPnl){preferredSize = new Dimension(0,80)}) = North
      layout(statBar) = South}) = South}
  contents = mainPanel
  //Methods
  def init() = {
    //Run niThread
    niThread.start()
    while(niThread.isAlive && (! NativeInterface.isOpen)){Thread.sleep(50)}
    //Set browser
    import java.awt.BorderLayout
    mainPanel.peer.add(browser, BorderLayout.CENTER)
    logger.info("Browser: "  + browser.getBrowserInfo)
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
    SwingUtilities.invokeLater(printRunnable)}
  def printStatus(sl:List[(String,String)]) = { //Arg: List(Name, Value)
    //Format
    def genSps(n:Int):String = (0 until n).map(_ => " ").mkString("")
    val fsl = sl.map{case(n,v) => {
      val s = if(n.size < 6){6}else{n.size}
      val nn = if(n.size < 6){n + genSps(6 - n.size)}else{n}
      val nv = if(v.size > s){v.take(s)}else if(v.size < s){v + genSps(s - v.size)}else {v}
      (nn, nv)}}
    //Print
    val (ns,vs) = fsl.unzip
    statBar.text = " " + ns.mkString(" ") + "\n" + " " + vs.mkString(" ")}}



