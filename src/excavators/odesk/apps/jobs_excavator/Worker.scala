package excavators.odesk.apps.jobs_excavator

import java.awt.image.BufferedImage
import java.io.File
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.parsers.HTMLJobParsers
import excavators.util.parameters.ParametersMap
import excavators.util.tasks.{TimedTaskExecutor, TimedTask}
import excavators.odesk.ui.{ManagedWorker, Browser}
import excavators.util.logging.Logger
import util.structures.{ParsedJob, ParsingErrorRow, FoundJobsRow}

/**
* Class encapsulate work logic
* Created by CAB on 13.10.2014.
*/

class Worker(browser:Browser, logger:Logger, saver:Saver, db:ODeskExcavatorsDBProvider) extends ManagedWorker with TimedTaskExecutor {
  //Parameters
  private var runAfterStart = false
  private val mainPageURL = "https://www.odesk.com"
  private val saveFolder = System.getProperty("user.home") + "\\Desktop"
  private val jobsFoundByAnaliseScrapTaskPriority = 1
  private val buildJobsScrapingTaskPriority = 2
  private val buildJobsScrapingTaskTimeout = 1000 * 60 * 30
  private val numberOfJobToScripInIteration = 200
  private var scrapTryMaxNumber = 10
  private var scrapTryTimeout = 5000
  private var buildJobsScrapingTimeout = 1000 * 20
  private val maxSaverQueueSize = 100
  //Variables
  private var excavatorNumber = -1
  private var numberOfProcJob = 0
  private var lastParsingQuality = 0.0
  private var numberOfFailureParsed = 0
  //Helpers
  private val htmlParser = new HTMLJobParsers
  //Construction
  super.setPaused(! runAfterStart)
  addTask(new BuildJobsScrapingTask(0))
  //Functions
  private def getImageByUrl(oUrl:Option[String], cut:List[Int]):Option[BufferedImage] = {
    oUrl.flatMap(url => {
      browser.getHTMLbyURL(url) match{
        case Some(html) if(html != "") => {
          Some(browser.captureImage.getSubimage(cut(0),cut(1),cut(2),cut(3)))}
        case _ => {
          logger.worn("[Worker.getImageByUrl] Failure on get image: " + url)
          None}}})}
  private def getAndParseJob(url:String):(Option[ParsedJob],Option[String]) = {
    browser.getHTMLbyURL(mainPageURL + url) match{
      case Some(html) if(html != "") => {
        (htmlParser.parseJob(html), Some(html))}
      case _ => {
        logger.worn("[Worker.getAndParseJob] No job html on: " + url)
        (None,None)}}}
  private def getFoundJobs(n:Int, en:Int):(List[FoundJobsRow],  Int) = {
    try{
      db.getNOfFoundByExcavatorNumber(n, en)}
    catch{case e:Exception => {
      logger.error("[Worker.getFoundJobs] Exception on getNOfOldFoundJobs: " + e)
      (List[FoundJobsRow](),0)}}}
  private def saveWrongParsedHtml(url:String, html:Option[String], pq:Double, cd:Date) = {
    val dr = ParsingErrorRow(
      id = 0,
      createDate = cd,
      oUrl = url,
      msg = "parsing quality = " + pq,
      html = html match{case Some(d) => d; case None => "No HTML."})
    try{
      db.addParsingErrorRow(dr)}
    catch{case e:Exception => {
      logger.error("[Worker.saveWrongParsedHtml] Exception on save parsing error html: " + e + ", url: " + url)}}}
  private def estimateQuality(pj:Option[ParsedJob], j:FoundJobsRow, html:Option[String], cd:Date):Double = {
    //Estimate parsing quality
    val pq = htmlParser.estimateParsingQuality(j, pj)
    lastParsingQuality = pq
    def blm(s:String):String = {
      val t = pj.map(_.job.jobTitle) match{case Some(t) => t; case None => "---"}
      "[Worker.estimateQuality] Job parsing " + s + ", pq = " + pq + ", url: " + j.oUrl + ", title: " + t}
    if(pq <= htmlParser.notSaveParsingQualityLevel){
      logger.error(blm("error, job not save") )}
    else if(pq <= htmlParser.errorParsingQualityLevel){
      logger.error(blm("error"))}
    else if(pq <= htmlParser.wornParsingQualityLevel){
      logger.worn(blm("worn"))}
    //Save source HTML if parsing quality low
    if(htmlParser.errorParsingQualityLevel > pq ||
      htmlParser.wornParsingQualityLevel > pq ||
      htmlParser.notSaveParsingQualityLevel > pq){
      numberOfFailureParsed += 1
      saveWrongParsedHtml(j.oUrl, html, pq, cd)}
    //Return
    pq}
  //Tasks
  case class BuildJobsScrapingTask(t:Long) extends TimedTask(t, buildJobsScrapingTaskPriority){def execute() = {
    //If sever not end work, then wait
    val na = if(saver.queueSize > maxSaverQueueSize){
      logger.worn("[Worker.BuildJobsScrapingTask] Saver is to slow.")
      0}
    else{
      //Get jobs
      val (js, _) = getFoundJobs(numberOfJobToScripInIteration, excavatorNumber)
      //Add scraping tasks
      js.foreach(j => {addTask(new JobsScraping(0,j,1))})
      js.size}
    //Logging
    logger.info("[Worker.BuildJobsScrapingTask] " + na + " new jobs added to scraping.")
    //If added no jobs, then wait
    if(na == 0){
      addTask(new BuildJobsScrapingTask(System.currentTimeMillis() + buildJobsScrapingTimeout))}}}
  case class JobsScraping(t:Long, j:FoundJobsRow, nScrapTry:Int) extends TimedTask(t, jobsFoundByAnaliseScrapTaskPriority){def execute() = {
    //Start
    logger.info("[Worker.BuildJobsScrapingTask] Start scrap,\n  url: " + j.oUrl)
    numberOfProcJob += 1
    //Add build jobs if no more in queue
    if(queueSize <= 1){addTask(new BuildJobsScrapingTask(0))}
    //Get and parse HTML
    val (pj, html) = getAndParseJob(j.oUrl)
    //Date to be use as 'created date'
    val cd = pj match{case Some(j) => j.job.createDate; case None => new Date}
    //Estimate parsing quality
    val pq = estimateQuality(pj,j,html,cd)
    //If job parsed good enough to save
    if(pj.nonEmpty && pq > htmlParser.notSaveParsingQualityLevel){
      //Get logo
      val l = getImageByUrl(pj.get.clientChanges.logoUrl, htmlParser.logoImageCoordinates)
      //Add saver task
      saver.addSaveJobDataAndDelFoundTask(j, pj.get, pq, cd, l)}
    else{
      //If parse wrong, add to next time (if no max try number)
      if(nScrapTry <= scrapTryMaxNumber){
        //Move pars task to future
        addTask(new JobsScraping((System.currentTimeMillis() + scrapTryTimeout), j, (nScrapTry + 1)))}
      else{
        //If max try worn and remove found job
        logger.worn("[Worker.JobsScraping] Failure with max scrape try (" + scrapTryMaxNumber + ").")
        saver.addDelFoundJobTask(j)}}}}
  //Methods
  def setParameters(p:ParametersMap) = {
    runAfterStart = p.getOrElse("runAfterStart", {
      logger.worn("[Worker.setParameters] Parameter 'runAfterStart' not found.")
      runAfterStart})
    scrapTryMaxNumber = p.getOrElse("scrapTryMaxNumber", {
      logger.worn("[Worker.setParameters] Parameter 'scrapTryMaxNumber' not found.")
      scrapTryMaxNumber})
    scrapTryTimeout = p.getOrElse("scrapTryTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'scrapTryTimeout' not found.")
      scrapTryTimeout})
    buildJobsScrapingTimeout = p.getOrElse("buildJobsScrapingTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'buildJobsScrapingTimeout' not found.")
      buildJobsScrapingTimeout})}
  def init(en:Int) = {
    excavatorNumber = en
    if(runAfterStart){logger.info("[Worker.init] Run on init.")}
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
      case Some(t) => {
        val p = saveFolder + "\\" + System.currentTimeMillis() + ".html"
        try{
          tools.nsc.io.File(p).writeAll(t)
          logger.info("[Worker.saveHtml] File save to: " + p)}
        catch{case e:Exception => {
          logger.error("[Worker.saveHtml] Exception when save: " + e)}}}
      case None => logger.worn("[Worker.saveHtml] Not save, is empty.")}}
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
    (queueSize,numberOfProcJob,lastParsingQuality,numberOfFailureParsed)}}

