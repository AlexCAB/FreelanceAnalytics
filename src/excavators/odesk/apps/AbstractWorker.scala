package excavators.odesk.apps

import java.awt.image.BufferedImage
import java.io.File
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.parsers.HTMLJobParsers
import excavators.odesk.ui.{Browser, ManagedWorker}
import excavators.util.logging.Logger
import excavators.util.tasks.TimedTaskExecutor
import util.structures.{ParsedJob, FoundJobsRow, ParsingErrorRow, FoundBy}

/**
 * Base class to implement job excavator workers
 * Created by CAB on 27.10.2014.
 */

class AbstractWorker(browser:Browser, logger:Logger, db:ODeskExcavatorsDBProvider) extends ManagedWorker with TimedTaskExecutor {
  //Parameters
  protected var runAfterStart = false
  protected val mainPageURL = "https://www.odesk.com"
  protected val saveFolder = System.getProperty("user.home") + "\\Desktop"
  protected val jobsFoundByAnaliseScrapTaskPriority = 1
  protected val buildJobsScrapingTaskPriority = 2
  protected var buildJobsScrapingTaskTimeout = 1000 * 60 * 30
  protected val numberOfJobToScripInIteration = 200
  protected var scrapTryMaxNumber = 10
  protected var scrapTryTimeout = 5000
  //Variables
  protected var numberOfProcJob = 0
  protected var lastParsingQuality = 0.0
  protected var numberOfFailureParsed = 0
  //Helpers
  protected val htmlParser = new HTMLJobParsers
  //Functions
  protected def getImageByUrl(oUrl:Option[String], cut:List[Int]):Option[BufferedImage] = {
    oUrl.flatMap(url => {
      browser.getHTMLbyURL(url) match{
        case Some(html) if(html != "") => {
          Some(browser.captureImage.getSubimage(cut(0),cut(1),cut(2),cut(3)))}
        case _ => {
          logger.worn("[Worker.getImageByUrl] Failure on get image: " + url)
          None}}})}
  protected def getAndParseJob(url:String):(Option[ParsedJob],Option[String]) = {
    browser.getHTMLbyURL(mainPageURL + url) match{
      case Some(html) if(html != "") => {
        (htmlParser.parseJob(html), Some(html))}
      case _ => {
        logger.worn("[Worker.getAndParseJob] No job html on: " + url)
        (None,None)}}}
  protected def getFoundJobs(n:Int, fb:FoundBy):(List[FoundJobsRow],  Int) = {
    try{
      db.getNOfFoundByJobs(n, fb)}
    catch{case e:Exception => {
      logger.error("[Worker.getFoundJobs] Exception on getNOfOldFoundJobs: " + e)
      (List[FoundJobsRow](),0)}}}
  protected def saveWrongParsedHtml(url:String, html:Option[String], pq:Double, cd:Date) = {
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
  protected def estimateQuality(pj:Option[ParsedJob], j:FoundJobsRow, html:Option[String], cd:Date):Double = {
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
  def init() = {
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
      logger.info("[Worker.setWork] Run.")}}}
