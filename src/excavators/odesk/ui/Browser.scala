package excavators.odesk.ui

import java.awt.image.BufferedImage
import java.util
import excavators.util.logging.Logger
import chrriis.dj.nativeswing.swtimpl.components.{WebBrowserNavigationParameters, JWebBrowser}
import javax.swing.SwingUtilities
import excavators.util.parameters.ParametersMap

/**
 * Native system browser wrapper.
 * Created by CAB on 15.10.2014.
 */

class Browser(logger:Logger) extends JWebBrowser {
  //Parameters(time in milli sec)
  private var loadPageMaxTime = 60000L
  private var loadPageTimeOut = 3000L
  private var loadTryMaxTime = 10000L
  private var confirmTimeOut = 2000L
  private var retryTimeOut = 1000L
  //Data
  private val webBrowser = this
  //Variables
  private var loadTime = 0L
  //Construction
  setMenuBarVisible(false)
  //Function9
  private val getHTMLbyURLNavigateRunnable = new Runnable(){
    var url = ""
    def run(){
      println(url)
      val p = new WebBrowserNavigationParameters
      val hs = new util.HashMap[String,String]()

      hs.put("Referer","https://www.odesk.com/users/Senior-Scala-Java-Android-iOS-Developer_~013a6f352dc7034896")
      hs.put("X-Odesk-Csrf-Token","dd181bc866f41bd96d451f5cda8d89c2")
      hs.put("X-Odesk-User-Agent","oDesk LM")
      hs.put("X-Requested-With","XMLHttpRequest")






      p.setHeaders(hs)
      url = "http://www.odesk.com/job-description/P7ixB8zT6MLHRpWe0hrKvfs3WIVYxr%2BSTMEma%2FltSrw%3D"

      navigate(url,p)

    }}
  private val captureImageRunnable = new Runnable(){
    var result:BufferedImage = _
    def run() = {
      val nc = getNativeComponent
      val os = nc.getSize
      val img = new BufferedImage(os.width, os.height, BufferedImage.TYPE_INT_RGB)
      nc.paintComponent(img)
      result = img}}
  private val getHTMLbyURLStatusRunnable = new Runnable(){
    var status = false
    def run(){
      status = (getLoadingProgress == 100)}} //getStatusText == "Done" &&
  private val getHTMLbyURLResultRunnable = new Runnable(){
    var content:Option[String] = None
    def run(){val r = getHTMLContent; if(r != null){content = Some(r)}}}
  private val getBrowserInfoRunnable = new Runnable(){
    var info:Option[String] = None
    def run(){info = Some("type = " + getBrowserType + ", version = " + getBrowserVersion)}}
  //Methods
  def setParameters(p:ParametersMap) = {
    loadPageMaxTime = p.getOrElse("loadPageMaxTime", {
      logger.worn("[Browser.setParameters] Parameter 'loadPageMaxTime' not found.")
      loadPageMaxTime})
    loadPageTimeOut = p.getOrElse("loadPageTimeOut", {
      logger.worn("[Browser.setParameters] Parameter 'loadPageTimeOut' not found.")
      loadPageTimeOut})
    loadTryMaxTime = p.getOrElse("loadTryMaxTime", {
      logger.worn("[Browser.setParameters] Parameter 'loadTryMaxTime' not found.")
      loadTryMaxTime})
    confirmTimeOut = p.getOrElse("confirmTimeOut", {
      logger.worn("[Browser.setParameters] Parameter 'confirmTimeOut' not found.")
      confirmTimeOut})
    retryTimeOut = p.getOrElse("retryTimeOut", {
      logger.worn("[Browser.setParameters] Parameter 'retryTimeOut' not found.")
      retryTimeOut})}
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
    //Start
    val tc = System.currentTimeMillis()
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
    //Load time
    loadTime = System.currentTimeMillis() - tc
    //Return
    getHTMLbyURLResultRunnable.content}
  def getBrowserInfo:String = getBrowserInfoRunnable.synchronized{
    getBrowserInfoRunnable.info = None
    if(SwingUtilities.isEventDispatchThread){
      getBrowserInfoRunnable.run()}
    else{
      SwingUtilities.invokeAndWait(getBrowserInfoRunnable)}
    getBrowserInfoRunnable.info.get}
  def captureImage:BufferedImage = captureImageRunnable.synchronized{
    if(SwingUtilities.isEventDispatchThread){
      captureImageRunnable.run()}
    else{
      SwingUtilities.invokeAndWait(captureImageRunnable)}
    captureImageRunnable.result}
  def getMetrics:Long = { //Return: Last HTML load time.
    loadTime}}
