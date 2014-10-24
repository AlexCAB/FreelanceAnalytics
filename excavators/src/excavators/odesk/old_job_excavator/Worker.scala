package excavators.odesk.old_job_excavator

import java.awt.image.BufferedImage
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.{Saver, DBProvider}
import excavators.odesk.structures._
import excavators.util.parameters.ParametersMap
import excavators.util.tasks.{TimedTaskExecutor,TimedTask}
import java.io.File
import excavators.odesk.parsers.HTMLJobParsers
import excavators.odesk.ui.{ManagedWorker, Browser}
import excavators.util.logging.Logger
import scala.math.random

/**
* Class encapsulate work logic
* Created by CAB on 13.10.2014.
*/

class Worker(browser:Browser, logger:Logger, saver:Saver, db:DBProvider) extends ManagedWorker with TimedTaskExecutor{
  //Parameters
  private var runAfterStart = false
  private val mainPageURL = "https://www.odesk.com"
  private val saveFolder = System.getProperty("user.home") + "\\Desktop"
  private val jobsFoundByAnaliseScrapTaskPriority = 1
  private val buildJobsScrapingTaskPriority = 2
  private var foundFreelancersPriority = 1
  private var jobsFoundByAnalisePriority = 1
  private var toTrackingJobPriority = 1
  private var buildJobsScrapingTaskTimeout = 1000 * 60 * 30
  private var nextJobCheckTimeout = 1000 * 60 * 60
  private val numberOfJobToScripInIteration = 200
  private var scrapTryMaxNumber = 10
  private var scrapTryTimeout = 5000
  //Variables
  private var numberOfProcJob = 0
  private var numberOfDoubleFoundJob = 0
  private var lastParsingQuality = 0.0
  private var numberOfFailureParsed = 0
  //Construction
  super.setPaused(! runAfterStart)
  addTask(new BuildJobsScrapingTask(System.currentTimeMillis()))
  val htmlParser = new HTMLJobParsers
  //Functions
  private def getFreelancerIdByUrl(oUrl:Option[String]):Option[Long] = {
    oUrl.flatMap(url => {
      try{
        db.getFreelancerIdByURL(url)}
      catch{case e:Exception => {
        logger.error("[Worker.getFreelancerIdByUrl] Exception: " + e)
        None}}})}
  private def getImageByUrl(oUrl:Option[String], cut:List[Int]):Option[BufferedImage] = {
    oUrl.flatMap(url => {
      browser.getHTMLbyURL(url) match{
        case Some(html) if(html != "") => {
          Some(browser.captureImage.getSubimage(cut(0),cut(1),cut(2),cut(3)))}
        case _ => {
          logger.worn("[Worker.getImageByUrl] Failure on get image: " + url)
          None}}})}
  private def getFoundJobs(n:Int, fb:FoundBy):(List[FoundJobsRow],  Int) = {
    try{
      db.getNOfFoundByJobs(n, fb)}
    catch{case e:Exception => {
      logger.error("[Worker.getFoundJobs] Exception on getNOfOldFoundJobs: " + e)
      (List[FoundJobsRow](),0)}}}
  private def checkIfJojAlreadyScraped(url:String):(Boolean,Option[(Long,JobAvailable)]) = { //(check successful, job is scraped id)
    try{
      (true, db.isJobScraped(url))}
    catch{case e:Exception => {
      logger.error("[Worker.checkIfJojAlreadyScraped] Exception on isJobScraped: " + e)
      (false,None)}}}
  private def updateNextCheckTime(id:Long) = {
    try{
      db.setNextJobCheckTime(id, Some(new Date(System.currentTimeMillis() + nextJobCheckTimeout)))}
    catch{case e:Exception => {
      logger.error("[Worker.addToTrackingTable] Exception on setNextJobCheckTime: " + e)}}}
  private def getAndParseJob(url:String):(Option[ParsedJob],Option[String]) = {
    browser.getHTMLbyURL(mainPageURL + url) match{
      case Some(html) if(html != "") => {
        (htmlParser.parseJob(html), Some(html))}
      case _ => {
        logger.worn("[Worker.getAndParseJob] No job html on: " + url)
        (None,None)}}}
  private def saveWrongParsedHtml(url:String, html:Option[String], pq:Double, cd:Date) = {
    val dr = ParsingErrorRow(
      id = 0,
      createDate = cd,
      oUrl = url,
      msg = "parsing quality = " + pq,
      html = html match{case Some(d) => d; case None => "No HTML."})
    try{
      val id = db.addParsingErrorRow(dr)}
    catch{case e:Exception => {
      logger.error("[Worker.saveWrongParsedHtml] Exception on save parsing error html: " + e + ", url: " + url)}}}
  private def saveMainJobData(j:FoundJobsRow, opj:Option[ParsedJob], pq:Double, cd:Date):Option[Long] = opj.flatMap(pj => {
    val ncd = new Date(cd.getTime + nextJobCheckTimeout)
    val jr = JobsRow(
      id = 0,
      foundData = j,
      daeDate = if(pj.jobChanges.jobAvailable == JobAvailable.No){Some(cd)}else{None}, //If job not available set dae date
      deleteDate = None,
      nextCheckDate = if(pj.jobChanges.jobAvailable != JobAvailable.No){Some(ncd)}else{None}, //If job not available not add to tracking
      jabData = pj.job)
    try{
      db.addJobsRow(jr) match {
        case Some(id) => {
          logger.info("[Worker.saveMainJobData] Added job: " + jr.foundData.oUrl + ", id = " + id + ", parsing quality = " + pq)
          Some(id)}
        case None => {
          logger.error("[Worker.saveMainJobData] Error on save job: " + jr.foundData.oUrl + ", already exist.")
          None}}}
    catch{case e:Exception => {
      logger.error("[Worker.saveMainJobData] Exception on save job: " + e + ", url=" + j.oUrl)
      None}}})
  private def prepareJobDataToSave(pj:Option[ParsedJob], jid:Option[Long], cd:Date, j:FoundJobsRow):
  Option[(JobsChangesRow, ClientsChangesRow, List[JobsApplicantsRow], List[JobsHiredRow],
  List[ClientsWorksHistoryRow], Set[FoundFreelancerRow], Set[FoundJobsRow])] = (pj,jid) match{
    case (Some(pj), Some(id)) => {
      //Prepare JobsChangesRow structure
      val jcr = JobsChangesRow(
        id = 0,
        jobId = id,
        changeData = pj.jobChanges)
      //Prepare ClientsChangesRow structure
      val ccr = ClientsChangesRow(
        id = 0,
        jobId = id,
        changeData = pj.clientChanges,
        logo = getImageByUrl(pj.clientChanges.logoUrl, htmlParser.logoImageCoordinates))
      //Prepare JobsApplicantsRow structures
      val ars = pj.applicants.map(a => {
        JobsApplicantsRow(
          id = 0,
          jobId = id,
          applicantData = a,
          freelancerId = getFreelancerIdByUrl(a.url))})
      //Prepare JobHired structures
      val jhr = pj.hires.map(h => {
        JobsHiredRow(
          id = 0,
          jobId = id,
          hiredData = h,
          freelancerId = getFreelancerIdByUrl(h.freelancerUrl))})
      //Prepare ClientsWorksHistoryRow structures
      val whr = pj.clientWorks.map(w => {
        ClientsWorksHistoryRow(
          id = 0,
          jobId = id,
          workData = w,
          freelancerId = getFreelancerIdByUrl(w.freelancerUrl))})
      //Prepare FoundFreelancerRow structures
      val ffr = {
        //Get freelancers urls
        val fus = pj.applicants.flatMap(_.url).toSet ++
          pj.hires.flatMap(_.freelancerUrl).toSet ++
          pj.clientWorks.flatMap(_.freelancerUrl).toSet
        //Filter which already exist
        fus.map(url => {
          FoundFreelancerRow(
            id = 0,
            oUrl = url,
            date = cd,
            priority = foundFreelancersPriority)})}
      //Prepare FoundJobsRow structures
      val fjr = {
        //Get work urls
        val fus = pj.clientWorks.flatMap(_.oUrl).toSet
        //Filter which already exist
        fus.map(url => {
          FoundJobsRow(
            id = 0,
            oUrl = url,
            foundBy = FoundBy.Analyse,
            date = cd,
            priority = {val np = j.priority - 1; if(np < 1){1}else{np}},
            skills = List(),
            nFreelancers = None)})}
      //Return
      Some((jcr,ccr,ars,jhr,whr,ffr,fjr))}
    case _ =>
      None}
  //Tasks
  case class BuildJobsScrapingTask(t:Long) extends TimedTask(t, buildJobsScrapingTaskPriority){def execute() = {
    //Get jobs
    val (js, _) = getFoundJobs(numberOfJobToScripInIteration, FoundBy.Analyse)
    //Add scraping tasks
    js.foreach(j => {addTask(new JobsScraping(0,j,1))})
    //Logging
    logger.info("[Worker.BuildJobsScrapingTask] " + js.size + " new jobs added to scraping.")
    //Add self to task set
    addTask(new BuildJobsScrapingTask(System.currentTimeMillis() + buildJobsScrapingTaskTimeout))}}
  case class JobsScraping(t:Long, j:FoundJobsRow, nScrapTry:Int) extends TimedTask(t, jobsFoundByAnaliseScrapTaskPriority){def execute() = {
    //Start
    logger.info("[Worker.BuildJobsScrapingTask] Start scrap url: " + j.oUrl)
    numberOfProcJob += 1
    //Add build jobs if no more in queue
    if(getNumTaskLike(this) == 0){addTask(new BuildJobsScrapingTask(0))}
    //Check if jib already scraped
    val cr = checkIfJojAlreadyScraped(j.oUrl) //(check successful, Option(scraped job id, job available))
    //Select activity
    cr match{
      case (true,Some((id:Long, ja:JobAvailable))) => {
        //Update next tracking time if job available
        if(ja != JobAvailable.No){updateNextCheckTime(id)}
        //Del from found jobs
        saver.addDelFoundJobTask(j)
        numberOfDoubleFoundJob += 1
        logger.info("[Worker.JobsScraping] Job already scraped, url = : " + j.oUrl)}
      case (true,None) => { //(job  not scraped, check successful)
        //Get and parse HTML
        val (pj, html) = getAndParseJob(j.oUrl)
        //Date to be use as 'created date'
        val cd = pj match{case Some(j) => j.job.createDate; case None => new Date}
        //Estimate parsing quality
        val pq = htmlParser.estimateParsingQuality(pj)
        lastParsingQuality = pq
        if(pq <= htmlParser.notSaveParsingQualityLevel){
          logger.error("[Worker.JobsScraping] Job parsing error, job not save, pq = " + pq + ", url: " + j.oUrl)}
        else if(pq <= htmlParser.errorParsingQualityLevel){
          logger.error("[Worker.JobsScraping] Job parsing error, pq = " + pq + ", url: " + j.oUrl)}
        else if(pq <= htmlParser.wornParsingQualityLevel){
          logger.worn("[Worker.JobsScraping] Job parsing worn, pq = " + pq + ", url: " + j.oUrl)}
        //Save source HTML if parsing quality low
        if(htmlParser.errorParsingQualityLevel > pq ||
          htmlParser.wornParsingQualityLevel > pq ||
          htmlParser.notSaveParsingQualityLevel > pq){
          numberOfFailureParsed += 1
          saveWrongParsedHtml(j.oUrl, html, pq, cd)}
        //If job parsed good enough to save
        if(pj.nonEmpty && pq > htmlParser.notSaveParsingQualityLevel){
          //Save job row, get job ID
          val jid = saveMainJobData(j,pj,pq, cd)
          //Remove from found if save successful
          jid.foreach(_ => {
            saver.addDelFoundJobTask(j)})
          //Prepare job data to save to DB
          prepareJobDataToSave(pj,jid,cd, j).map(prs => {
            //Safe rows to DB and and remove from found
            saver.addSaveJobAdditionalDataAndDelFoundTask(prs)})}
        else{
          //If parse wrong, add to next time (if no max try number)
          if(nScrapTry <= scrapTryMaxNumber){
            //Move pars task to future
            addTask(new JobsScraping((System.currentTimeMillis() + scrapTryTimeout), j, (nScrapTry + 1)))}
          else{
            //If max try worn and remove found job
            logger.worn("[Worker.JobsScraping] Failure with max scrape try (" + scrapTryMaxNumber + ").")
            saver.addDelFoundJobTask(j)}}}
      case _ =>}}}
  //Methods
  def setParameters(p:ParametersMap) = {
    runAfterStart = p.getOrElse("runAfterStart", {
      logger.worn("[Worker.setParameters] Parameter 'runAfterStart' not found.")
      runAfterStart})
    foundFreelancersPriority = p.getOrElse("foundFreelancersPriority", {
      logger.worn("[Worker.setParameters] Parameter 'foundFreelancersPriority' not found.")
      foundFreelancersPriority})
    jobsFoundByAnalisePriority = p.getOrElse("jobsFoundByAnalisePriority", {
      logger.worn("[Worker.setParameters] Parameter 'jobsFoundByAnalisePriority' not found.")
      jobsFoundByAnalisePriority})
    toTrackingJobPriority = p.getOrElse("toTrackingJobPriority", {
      logger.worn("[Worker.setParameters] Parameter 'toTrackingJobPriority' not found.")
      toTrackingJobPriority})
    buildJobsScrapingTaskTimeout = p.getOrElse("buildJobsScrapingTaskTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'buildJobsScrapingTaskTimeout' not found.")
      buildJobsScrapingTaskTimeout})
    nextJobCheckTimeout = p.getOrElse("nextJobCheckTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'nextJobCheckTimeout' not found.")
      nextJobCheckTimeout})
    scrapTryMaxNumber = p.getOrElse("scrapTryMaxNumber", {
      logger.worn("[Worker.setParameters] Parameter 'scrapTryMaxNumber' not found.")
      scrapTryMaxNumber})
    scrapTryTimeout = p.getOrElse("scrapTryTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'scrapTryTimeout' not found.")
      scrapTryTimeout})}
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
      logger.info("[Worker.setWork] Run.")}}
  def getMetrics:(Int,Int,Int,Double,Int) = {
    (queueSize,numberOfProcJob,numberOfDoubleFoundJob,lastParsingQuality,numberOfFailureParsed)}}

