package excavators.odesk

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser
import javax.swing.SwingUtilities


object Browser extends JWebBrowser {
  //Parameters(time in milli sec)
  val loadPageMaxTime = 60000
  val loadPageTimeOut = 3000
  val loadTryMaxTime = 10000
  val confirmTimeOut = 2000
  val retryTimeOut = 1000
  //Construction
  setMenuBarVisible(false)
  //Function
  private val getHTMLbyURLNavigateRunnable = new Runnable(){
    var url = ""
    def run(){navigate(url)}}
  private val getHTMLbyURLStatusRunnable = new Runnable(){
    var status = false
    def run(){
      status = (getStatusText == "Done" && getLoadingProgress == 100)}}
  private val getHTMLbyURLResultRunnable = new Runnable(){
    var content:Option[String] = None
    def run(){val r = getHTMLContent; if(r != null){content = Some(r)}}}
  private val getBrowserInfoRunnable = new Runnable(){
    var info:Option[String] = None
    def run(){info = Some("type = " + getBrowserType + ", version = " + getBrowserVersion)}}
  //Methods
  def openURL(url:String) = getHTMLbyURLNavigateRunnable.synchronized{
    getHTMLbyURLNavigateRunnable.url = url
    SwingUtilities.invokeLater(getHTMLbyURLNavigateRunnable)}
  def getHTMLbyURL(url:String):Option[String] = getHTMLbyURLResultRunnable.synchronized{
    //Three load try
    getHTMLbyURLResultRunnable.content = None
    var ltc = 3
    while(ltc > 0 && getHTMLbyURLResultRunnable.content.isEmpty){
      //Start load
      getHTMLbyURLNavigateRunnable.url = url
      SwingUtilities.invokeAndWait(getHTMLbyURLNavigateRunnable)
      //Time out
      Thread.sleep(loadPageTimeOut)
      //Wait load
      var ni = loadTryMaxTime / 50
      while(ni > 0){
        getHTMLbyURLStatusRunnable.status = false
        SwingUtilities.invokeAndWait(getHTMLbyURLStatusRunnable)
        if(getHTMLbyURLStatusRunnable.status){
          Thread.sleep(loadPageTimeOut)
          getHTMLbyURLStatusRunnable.status = false
          SwingUtilities.invokeAndWait(getHTMLbyURLStatusRunnable)}
        if(getHTMLbyURLStatusRunnable.status){
          ni = -1}
        else{
          Thread.sleep(50)}
        ni -= 1}
      //Return or retry
      if(ni < 0){
        SwingUtilities.invokeAndWait(getHTMLbyURLResultRunnable)}
      //Next try
        ltc -= 1}
    //Return
    getHTMLbyURLResultRunnable.content}
  def getBrowserInfo():String = getBrowserInfoRunnable.synchronized{
    getBrowserInfoRunnable.info = None
    SwingUtilities.invokeAndWait(getBrowserInfoRunnable)
    getBrowserInfoRunnable.info.get}}
