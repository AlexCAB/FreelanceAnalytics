package excavators.odesk.ui

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser
import javax.swing.SwingUtilities


class Browser extends JWebBrowser {
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
  def getCurrentHTML:Option[String] = getHTMLbyURLResultRunnable.synchronized{
    getHTMLbyURLResultRunnable.content = None
    if(SwingUtilities.isEventDispatchThread){
      getHTMLbyURLResultRunnable.run()}
    else{
      SwingUtilities.invokeAndWait(getHTMLbyURLResultRunnable)}
    getHTMLbyURLResultRunnable.content}
  def getHTMLbyURL(url:String):Option[String] = getHTMLbyURLResultRunnable.synchronized{
    //Three load try
    getHTMLbyURLResultRunnable.content = None
    var ltc = 3
    while(ltc > 0 && getHTMLbyURLResultRunnable.content.isEmpty){
      //Start load
      getHTMLbyURLNavigateRunnable.url = url
      if(SwingUtilities.isEventDispatchThread){
        getHTMLbyURLNavigateRunnable.run()}
      else{
        SwingUtilities.invokeAndWait(getHTMLbyURLNavigateRunnable)}
      //Time out
      Thread.sleep(loadPageTimeOut)
      //Wait load
      var ni = loadTryMaxTime / 50
      while(ni > 0){
        getHTMLbyURLStatusRunnable.status = false
        if(SwingUtilities.isEventDispatchThread){
          getHTMLbyURLStatusRunnable.run()}
        else{
          SwingUtilities.invokeAndWait(getHTMLbyURLStatusRunnable)}
        if(getHTMLbyURLStatusRunnable.status){
          Thread.sleep(loadPageTimeOut)
          getHTMLbyURLStatusRunnable.status = false
          if(SwingUtilities.isEventDispatchThread){
            getHTMLbyURLStatusRunnable.run()}
          else{
            SwingUtilities.invokeAndWait(getHTMLbyURLStatusRunnable)}}
        if(getHTMLbyURLStatusRunnable.status){
          ni = -1}
        else{
          Thread.sleep(50)}
        ni -= 1}
      //Return or retry
      if(ni < 0){
        if(SwingUtilities.isEventDispatchThread){
          getHTMLbyURLResultRunnable.run()}
        else{
          SwingUtilities.invokeAndWait(getHTMLbyURLResultRunnable)}}
      //Next try
        ltc -= 1}
    //Return
    getHTMLbyURLResultRunnable.content}
  def getBrowserInfo():String = getBrowserInfoRunnable.synchronized{
    getBrowserInfoRunnable.info = None
    if(SwingUtilities.isEventDispatchThread){
      getBrowserInfoRunnable.run()}
    else{
      SwingUtilities.invokeAndWait(getBrowserInfoRunnable)}
    getBrowserInfoRunnable.info.get}}