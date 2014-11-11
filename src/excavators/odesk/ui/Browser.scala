package excavators.odesk.ui

import java.awt.image.BufferedImage
import util.logging.Logger
import chrriis.dj.nativeswing.swtimpl.components.{WebBrowserNavigationParameters, JWebBrowser}
import javax.swing.SwingUtilities

import util.parameters.ParametersMap


/**
 * Native system browser wrapper.
 * Created by CAB on 15.10.2014.
 */

class Browser(logger:Logger) extends JWebBrowser {
  //Parameters(time in milli sec)
  private var loadPageMaxTime = 60000L
  private var loadPageTimeOut = 4000L
  private var loadTryMaxTime = 10000L
  private var confirmTimeOut = 4000L
  private var retryTimeOut = 1000L
  private var jsonLoadTimeOut = 500L
  private var jsonLoadMaxTry = 5
  //Data
  private val webBrowser = this
  //Variables
  private var loadTime = 0L
  //Construction
  setMenuBarVisible(false)
  //Function9
  private val getHTMLbyURLNavigateRunnable = new Runnable(){
    var url = ""
    def run(){navigate(url)}}
  private val captureImageRunnable = new Runnable(){
    var result:BufferedImage = _
    def run() = {
      val nc = getNativeComponent
      val os = nc.getSize
      val img = new BufferedImage(os.width, os.height, BufferedImage.TYPE_INT_RGB)
      nc.paintComponent(img)
      result = img}}
  private val getJsonRunnable = new Runnable(){
    var url:Option[String] = None
    var result:Option[String] = None
    def run() = if(url.nonEmpty){
      //Inject JS
      val ir = webBrowser.executeJavascriptWithResult(
        """
          |//Create buffer if notexist
          |if(typeof jsonBuffer === 'undefined'){
          |  var s = document.createElement('script');
          |  s.type = 'text/javascript';
          |  var code = 'var jsonBuffer = \"\";';
          |  try {
          |    s.appendChild(document.createTextNode(code));
          |    document.body.appendChild(s);}
          |  catch (e) {
          |    s.text = code;
          |    document.body.appendChild(s);}}
          |else{
          |  jsonBuffer = '';}
          |//Run loading task
          |function getJson(){
          |  odesk.ajax({
          |    rawValues:[-1],
        """.stripMargin +
          "url:'" + url.get + "'," +
        """
          |    success:function(e){
          |      jsonBuffer = odesk.json.stringify(e);}});}
          |setTimeout(getJson, 100);
          |return 'ok';
        """.stripMargin)
      //Wait result if successful
      if(ir == "ok"){
        Thread.sleep(jsonLoadTimeOut)
        var i = 0
        while(i < jsonLoadMaxTry && result.isEmpty){
          webBrowser.executeJavascriptWithResult("return jsonBuffer;") match{
            case s:String if (s != "" && s.head == '{') ⇒ {result = Some(s)}
            case _ => Thread.sleep(jsonLoadTimeOut)}
        i += 1}}}}
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
      retryTimeOut})
    jsonLoadTimeOut = p.getOrElse("jsonLoadTimeOut", {
      logger.worn("[Browser.setParameters] Parameter 'jsonLoadTimeOut' not found.")
      jsonLoadTimeOut})
    jsonLoadMaxTry = p.getOrElse("jsonLoadMaxTry", {
      logger.worn("[Browser.setParameters] Parameter 'jsonLoadMaxTry' not found.")
      jsonLoadMaxTry})}
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
  def getJSONByUrl(url:String):Option[String] = getJsonRunnable.synchronized{
    getJsonRunnable.result = None
    getJsonRunnable.url = Some(url)
    if(SwingUtilities.isEventDispatchThread){
      getJsonRunnable.run()}
    else{
      SwingUtilities.invokeAndWait(getJsonRunnable)}
    getJsonRunnable.result}
  def getMetrics:Long = { //Return: Last HTML load time.
    loadTime}}












































