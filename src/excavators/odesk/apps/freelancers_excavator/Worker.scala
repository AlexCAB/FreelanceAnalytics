package excavators.odesk.apps.freelancers_excavator

import java.awt.image.BufferedImage
import java.io.File
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.parsers.{HTMLFreelancerParser, HTMLJobParsers}
import excavators.odesk.ui.{ManagedWorker, Browser}
import util.logging.Logger
import util.parameters.ParametersMap
import util.structures._
import util.tasks.{TimedTask, TimedTaskExecutor}

/**
* Class encapsulate work logic
* Created by CAB on 19.11.2014.
*/

class Worker(browser:Browser, logger:Logger, saver:Saver, db:ODeskExcavatorsDBProvider) extends ManagedWorker with TimedTaskExecutor {
  //Parameters
  private val mainPageURL = "https://www.odesk.com"
  private val workJsonURL = "/job-description/"
  private val portfolioJsonURL1 = "/fwh-api/project/id/"
  private val portfolioJsonURL2 = "/key/"
  private val saveFolder = System.getProperty("user.home") + "\\Desktop"
  private val ScrapTaskPriority = 1
  private val buildScrapingTaskPriority = 2
  private val numberOfToScripInIteration = 200
  private var scrapFreelancerTryMaxNumber = 3
  private var scrapFreelancerTimeout = 5000
  private var buildFreelancersScrapingTimeout = 1000 * 120
  private val maxSaverQueueSize = 100
  //Variables
  private var excavatorNumber = -1
  private var numberOfProc = 0
  private var lastParsingQuality = 0.0
  private var numberOfFailureParsed = 0
  //Helpers
  private val htmlParser = new HTMLFreelancerParser
  //Construction
  super.setPaused(true)
  addTask(new BuildScrapingTask(0))
  //Functions
  private def getFoundFreelancers(n:Int, en:Int):(List[FoundFreelancerRow], Int) = {
    try{
      db.getNOfFoundFreelancersByExcavatorNumber(n, en)}
    catch{case e:Exception => {
      logger.error("[Worker.getFoundFreelancers] Exception on getNOfOldFoundFreelancers: " + e)
      (List[FoundFreelancerRow](),0)}}}
  private def getAndParseFreelancerProfile(url:String):(Option[FreelancerParsedData],Option[String]) = {
    browser.getHTMLbyURL(mainPageURL + url) match{
      case Some(html) if(html != "") ⇒ {
        (Some(htmlParser.parseFreelancerProfile(html)),Some(html))}
      case _ ⇒ {
        logger.worn("[Worker.getAndParseFreelancerProfile] No html on: " + url)
        (None,None)}}}
  private def saveWrongParsedHtml(url:String, html:Option[String], pq:Double, cd:Date) = {
    val dr = ParsingErrorRow(
      id = 0,
      createDate = cd,
      oUrl = url,
      msg = "parsing quality = " + pq,
      html = html match{case Some(d) ⇒ d; case None ⇒ "No HTML."})
    try{
      db.addParsingErrorRow(dr)}
    catch{case e:Exception ⇒ {
      logger.error("[Worker.saveWrongParsedHtml] Exception on save parsing error html: " + e + ", url: " + url)}}}
  private def estimateParsingQuality(f:FoundFreelancerRow, pd:Option[FreelancerParsedData], html:Option[String], cd:Date):Double = {
    //Estimate parsing quality
    val pq = htmlParser.estimateParsingQuality(f, pd)
    lastParsingQuality = pq
    def blm(s:String):String = {
      val t = pd.map(d ⇒ d.changes.title) match{case Some(t) ⇒ t; case None ⇒ "---"}
      "[Worker.estimateQuality] Freelancer parsing " + s + ", pq = " + pq + ", url: " + f.oUrl + ", title: " + t}
    if(pq <= htmlParser.notSaveParsingQualityLevel){
      logger.error(blm("error, not save") )}
    else if(pq <= htmlParser.errorParsingQualityLevel){
      logger.error(blm("error"))}
    else if(pq <= htmlParser.wornParsingQualityLevel){
      logger.worn(blm("worn"))}
    //Save source HTML if parsing quality low
    if(htmlParser.errorParsingQualityLevel > pq ||
      htmlParser.wornParsingQualityLevel > pq ||
      htmlParser.notSaveParsingQualityLevel > pq){
      numberOfFailureParsed += 1
      saveWrongParsedHtml(f.oUrl, html, pq, cd)}
    //Return
    pq}
  private def getAdditionalWorkData(ks:List[String]):Map[String,FreelancerWorkData] = {
    ks.foldLeft(Map[String,FreelancerWorkData]())((wds, k) ⇒ {
      val url = workJsonURL + k
      val wd = browser.getJSONByUrl(url) match{
        case Some(j) if j != "" ⇒  Some(htmlParser.parseWorkJson(j))
        case _ ⇒ None}
      wd match{
        case Some(d) ⇒ wds + (k → d)
        case None ⇒ {
          logger.worn("[Worker.getAdditionalWorkData] Failure on get, key: " + k)
          wds}}})}
  private def getAdditionalPortfolioData(ks:List[String], fId:String):Map[String,FreelancerPortfolioData] = {
    ks.foldLeft(Map[String,FreelancerPortfolioData]())((wds, id) ⇒ {
      val url = portfolioJsonURL1 + id + portfolioJsonURL2 + fId
      val wd = browser.getJSONByUrl(url) match{
        case Some(j) if j != "" ⇒  Some(htmlParser.parsePortfolioJson(j))
        case _ ⇒ None}
      wd match{
        case Some(d) ⇒ wds + (id → d)
        case None ⇒ {
          logger.worn("[Worker.getAdditionalPortfolioData] Failure on get, url: " + url)
          wds}}})}
  private def getImageByUrl(oUrl:Option[String], cut:List[Int]):Option[BufferedImage] = {
    oUrl.flatMap(url ⇒ {
      browser.getHTMLbyURL(url) match{
        case Some(html) if(html != "") ⇒ {
          Some(browser.captureImage.getSubimage(cut(0),cut(1),cut(2),cut(3)))}
        case _ ⇒ {
          logger.worn("[Worker.getImageByUrl] Failure on get image: " + url)
          None}}})}
  //Tasks
  case class BuildScrapingTask(t:Long) extends TimedTask(t, buildScrapingTaskPriority){def execute() = {
    //If sever not end work, then wait
    val na = if(saver.queueSize > maxSaverQueueSize){
      logger.worn("[Worker.BuildScrapingTask] Saver is to slow, no freelancers added.")
      0}
    else{
      //Get
      val (fs, _) = getFoundFreelancers(numberOfToScripInIteration, excavatorNumber)
      //Add scraping tasks
      fs.foreach(f ⇒ {addTask(new Scraping(0,f,1))})
      logger.info("[Worker.BuildScrapingTask] " + fs.size + " new freelancers added to scraping.")
      fs.size}
    //If added no, then wait
    if(na == 0){
      addTask(new BuildScrapingTask(System.currentTimeMillis() + buildFreelancersScrapingTimeout))}}}
  case class Scraping(t:Long, f:FoundFreelancerRow, nScrapTry:Int) extends TimedTask(t, ScrapTaskPriority){def execute() = {
    //Start
    logger.info("[Worker.Scraping] Start scrap,\n  url: " + f.oUrl)
    numberOfProc += 1
    //Add build if no more in queue
    if(queueSize <= 1){addTask(new BuildScrapingTask(0))}
    //Get and parse HTML
    val (pd, html) = getAndParseFreelancerProfile(f.oUrl)
    //Date to be use as 'created date'
    val cd = pd match{case Some(d) ⇒ d.changes.createDate; case None ⇒ new Date}
    //Estimate parsing quality
    val pq = estimateParsingQuality(f, pd, html, cd)
    //If parsed good enough to save
    if(pd.nonEmpty && html.nonEmpty && pq > htmlParser.notSaveParsingQualityLevel){
      val (d,h) = (pd.get, html.get)
      //Get additional json data
      val ws = getAdditionalWorkData(d.works.flatMap(_.jobKey))
      val fId = "~" + f.oUrl.split("%").last.drop(2)
      val ps = getAdditionalPortfolioData(d.portfolio.flatMap(_.dataId), fId)
      //Get images
      val pi = getImageByUrl(d.changes.photoUrl, htmlParser.photoImageCoordinates)
      val li = getImageByUrl(d.changes.companyLogoUrl, htmlParser.companyLogoImageCoordinates)
      val cis = ws.flatMap(_._2.clientProfileLogo).flatMap(url ⇒ {
        getImageByUrl(Some(url), htmlParser.clientProfileLogoImageCoordinates).map(i ⇒ (url, i))}).toMap
      //Add saver task
      saver.addSaveFreelancerDataAndDelFoundTask(f,d,h,ws,ps,pi,li,cis)}
    else{
      if(nScrapTry <= scrapFreelancerTryMaxNumber){  //If parse wrong, add to next time (if no max try number)
        addTask(new Scraping((System.currentTimeMillis() + scrapFreelancerTimeout), f, (nScrapTry + 1)))} //Move pars task to future
      else{
        logger.worn("[Worker.Scraping] Failure with max scrape try (" + scrapFreelancerTryMaxNumber + ").") //If max try worn and remove found
        saver.addDelFoundFreelancerTask(f)}}}}
  //Methods
  def setParameters(p:ParametersMap) = {
     scrapFreelancerTryMaxNumber = p.getOrElse("scrapFreelancerTryMaxNumber", {
      logger.worn("[Worker.setParameters] Parameter 'scrapFreelancerTryMaxNumber' not found.")
      scrapFreelancerTryMaxNumber})
    scrapFreelancerTimeout = p.getOrElse("scrapFreelancerTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'scrapFreelancerTimeout' not found.")
      scrapFreelancerTimeout})
    buildFreelancersScrapingTimeout = p.getOrElse("buildFreelancersScrapingTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'buildFreelancersScrapingTimeout' not found.")
      buildFreelancersScrapingTimeout})}
  def init(en:Int) = {
    excavatorNumber = en
    start()}
  def halt() = {
    stop()
    logger.info("[Worker.init] Stop.")}
  def goToMain() = {
    if(isPaused){
      browser.openURL(mainPageURL)}
    else{
      logger.worn("[Worker.goToMain] Can't go to main page when work.")}}
  def saveHtml() = {
    browser.getCurrentHTML match{
      case Some(t) ⇒ {
        val p = saveFolder + "\\" + System.currentTimeMillis() + ".html"
        try{
          tools.nsc.io.File(p).writeAll(t)
          logger.info("[Worker.saveHtml] File save to: " + p)}
        catch{case e:Exception ⇒ {
          logger.error("[Worker.saveHtml] Exception when save: " + e)}}}
      case None ⇒ logger.worn("[Worker.saveHtml] Not save, is empty.")}}
  def saveScreenshot() = {
    val img = browser.captureImage
    val p = saveFolder + "\\" + System.currentTimeMillis() + ".png"
    val f = new File(p)
    ImageIO.write(img, "PNG", f)
    logger.info("[Worker.saveScreenshot] File save to: " + p)}
  override def setPaused(s:Boolean) = {
    super.setPaused(s)
    if(s){
      logger.info("[Worker.setWork] Paused.")}
    else{
      logger.info("[Worker.setWork] Run.")}}
  def getMetrics:(Int,Int,Double,Int) = {
    (queueSize,numberOfProc,lastParsingQuality,numberOfFailureParsed)}}

