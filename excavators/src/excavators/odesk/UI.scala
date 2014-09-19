package excavators.odesk

import scala.collection.mutable.{Map => MutMap, Set => MutSet, ListBuffer => MutList}
import chrriis.dj.nativeswing.swtimpl.NativeInterface
import scala.swing._
import scala.swing.event._
import java.text.SimpleDateFormat
import java.util.Date


object UI extends Frame {
  //Parameters
  val maxLogSize = 100
  //Variables
  private val logList = MutList[String]()
  private var ready = false
  //Construction
  title = "oDesk excavator"
  preferredSize = new Dimension(1000, 600)
  private val dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
  override def closeOperation():Unit = {Main.termination()}
  //Init thread
  private val niThread = new Thread{
    override def run():Unit = {
      NativeInterface.open()
      NativeInterface.runEventPump()}}
  //Components
  private val logger = new TextArea(){editable = false}
  //UI
  private val mainPanel = new BorderPanel {
    import BorderPanel.Position._
    import FlowPanel.Alignment._
    layout(new FlowPanel(Left)(){
      contents += new Button("To main"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          Worker.goToMain()}}
      contents += new Button("Get HTML"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          Worker.saveHtml()}}
      contents += new Button("RUN"){
        preferredSize = new Dimension(100,20)
        reactions += {case ButtonClicked(_) =>
          Worker.setWork(! Worker.work)
          text = if(Worker.work){"STOP"}else{"RUN"}}}}) = North
    layout(new ScrollPane(logger){preferredSize = new Dimension(0,80)}) = South}
  contents = mainPanel
  //Methods
  def init() = {
    //Run niThread
    niThread.start()
    while(niThread.isAlive && (! NativeInterface.isOpen)){Thread.sleep(50)}
    //Set browser
    import java.awt.BorderLayout
    mainPanel.peer.add(Browser, BorderLayout.CENTER)
    Logger.log("Browser: "  + Browser.getBrowserInfo())
    //Show
    visible = true
    ready = true}
  def halt() = {
    NativeInterface.close()
    peer.dispose()}
  def addLogMsg(date:Date, msg:String) = if(ready){
    logList += ("[" + dateFormat.format(date) + "] " + msg)
    if(logList.size > maxLogSize){logList.remove(0)}
    logger.text = logList.mkString("\n")}}



