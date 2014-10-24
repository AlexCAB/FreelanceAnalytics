package excavators.odesk.new_job_excavator

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
  private var jobSearchURL = "https://www.odesk.com/jobs/?q="
  private val saveFolder = System.getProperty("user.home") + "\\Desktop"
  private val buildJobsScrapingTaskPriority = 3
  private val searchNewJobsTaskPriority = 2
  private val jobsFoundBySearchScrapTaskPriority = 1
  private var foundFreelancersPriority = 100
  private var jobsFoundBySearchPriority = 100
  private var jobsFoundByAnalisePriority = 100
  private var toTrackingJobPriority = 1
  private var searchNewJobTimeout = 1000 * 60 * 50
  private var buildJobsScrapingTaskTimeout = 1000 * 60 * 30
  private var nextJobCheckTimeout = 1000 * 60 * 60
  private val numberOfJobToScripInIteration = 200
  private var maxNumberOfCheckedJob = 100
  private var scrapTryMaxNumber = 10
  private var scrapTryTimeout = 5000
  //Variables
  private var numberOfFoundJob = 0
  private var numberOfFoundJobInDB = 0
  private var numberOfProcJob = 0
  private var lastParsingQuality = 0.0
  private var numberOfFailureParsed = 0
  //Construction
  super.setPaused(! runAfterStart)
  addTask(new BuildJobsScrapingTask(System.currentTimeMillis()))
  addTask(new SearchNewJobs(System.currentTimeMillis()))
  val htmlParser = new HTMLJobParsers
  //Functions
  private def saveFoundJobsToDB(fj:List[FoundWork]):(Int,Boolean) = { //Return (N saved, Has already exist in DB)
    //Preparing jobs to save
    val pj = fj.map(j => FoundJobsRow(
      id = 0,
      oUrl = j.url,
      foundBy = FoundBy.Search,
      date = new Date,
      priority = jobsFoundBySearchPriority,
      skills = j.skills,
      nFreelancers = j.nFreelancers))
    //Save jobs to DB
    val ads = pj.map(j => {
      try{
        if(db.addFoundJobsRow(j)){1}else{0}}
      catch{case e:Exception => {
        logger.error("[Worker.saveFoundJobsToDB] Job " + j.oUrl + " not added: " + e)
        0}}})
    val na = ads.sum
    //Return n added
    (na, (fj.size != na))}
  private def searchAndSaveJobs():(Int,Int) = { //Return: (number of found, number of collected)
    //Get first page
    browser.getHTMLbyURL(jobSearchURL) match{
      case Some(fHtml) if(fHtml != "") => {
        //Parse search result HTML
        val fsr = htmlParser.parseWorkSearchResult(fHtml)
        if(fsr.works.nonEmpty){
          var (na, nk) = saveFoundJobsToDB(fsr.works)
          logger.info("[Worker.searchAndSaveJobs] On start collect " + na + " jobs.")
          //Collect next to known or max N work
          var nUrl = fsr.nextUrl
          var nChk = fsr.works.size
          while((! nk) && nChk < maxNumberOfCheckedJob && nUrl.nonEmpty){
            //Get next page
            browser.getHTMLbyURL(mainPageURL + nUrl.get) match {
              case Some(html) if (html != "") => {
                val sr = htmlParser.parseWorkSearchResult(html)
                if(sr.works.nonEmpty){
                  nChk += sr.works.size
                  val (nna, nnk) = saveFoundJobsToDB(sr.works)
                  na += nna
                  nk = nnk
                  logger.info("[Worker.searchAndSaveJobs] On next collect " + nna + " jobs, total " + na + "jobs.")}
                else{
                  logger.worn("[Worker.searchAndSaveJobs] No job in next search result(parse error), at: " + nUrl)}
                nUrl = sr.nextUrl}
              case _ => {
                logger.worn("[Worker.searchAndSaveJobs] No next job search result at: " + nUrl)
                nUrl = None}}}
          if(nChk >= maxNumberOfCheckedJob){
            logger.worn("[Worker.searchAndSaveJobs] More jobs then max (" + maxNumberOfCheckedJob + ")")}
          //Return N found and N added
          (fsr.nFound.getOrElse(0), na)}
        else{
          logger.worn("[Worker.searchAndSaveJobs] No job in search result(parse error).")
          (0,0)}}
      case _ => {
        logger.worn("[Worker.searchAndSaveJobs] No job search result.")
        (0,0)}}}
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
  private def prepareJobDataToSave(pj:Option[ParsedJob], jid:Option[Long], cd:Date):
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
            priority = jobsFoundByAnalisePriority,
            skills = List(),
            nFreelancers = None)})}
      //Return
      Some((jcr,ccr,ars,jhr,whr,ffr,fjr))}
    case _ =>
      None}
  //Tasks
  case class SearchNewJobs(t:Long) extends TimedTask(t, searchNewJobsTaskPriority){def execute() = {
    //Log
    logger.info("[Worker.SearchNewJobs] Start search of new jom.")
    //Search works
    val (nf,nc) = searchAndSaveJobs()
    //Add BuildJobsScrapingTask task if added some jobs.
    if(nc != 0){
      addTask(new BuildJobsScrapingTask(System.currentTimeMillis()))}
    //Add next task
    val mt = searchNewJobTimeout / 5
    val nt = ((searchNewJobTimeout - mt) * random).toInt + mt
    numberOfFoundJob += nc
    logger.info("[Worker.SearchNewJobs] End searching of new job, next via " + (nt / 1000) +
      " sec., found " + nf + ", collected " + nc + " jobs.")
    addTask(new SearchNewJobs(nt + System.currentTimeMillis()))}}
  case class BuildJobsScrapingTask(t:Long) extends TimedTask(t, buildJobsScrapingTaskPriority){def execute() = {
    //Get jobs
    val (js, n) = getFoundJobs(numberOfJobToScripInIteration, FoundBy.Search)
    numberOfFoundJobInDB = n
    //Add scraping tasks
    js.foreach(j => {addTask(new JobsScraping(0,j,1))})
    //Logging
    logger.info("[Worker.BuildJobsScrapingTask] " + js.size + " new jobs added to scraping.")
    //Add self to task set
    addTask(new BuildJobsScrapingTask(System.currentTimeMillis() + buildJobsScrapingTaskTimeout))}}
  case class JobsScraping(t:Long, j:FoundJobsRow, nScrapTry:Int) extends TimedTask(t, jobsFoundBySearchScrapTaskPriority){def execute() = {
    //Start
    logger.info("[Worker.BuildJobsScrapingTask] Start scrap url: " + j.oUrl)
    numberOfProcJob += 1
    //Add build jobs if no more in queue
    if(getNumTaskLike(this) == 0){addTask(new BuildJobsScrapingTask(0))}
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
      prepareJobDataToSave(pj,jid, cd).map(prs => {
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
        saver.addDelFoundJobTask(j)}}}}
  //Methods
  def setParameters(p:ParametersMap) = {
    runAfterStart = p.getOrElse("runAfterStart", {
      logger.worn("[Worker.setParameters] Parameter 'runAfterStart' not found.")
      runAfterStart})
    jobSearchURL = p.getOrElse("jobSearchURL", {
      logger.worn("[Worker.setParameters] Parameter 'jobSearchURL' not found.")
      jobSearchURL})
    foundFreelancersPriority = p.getOrElse("foundFreelancersPriority", {
      logger.worn("[Worker.setParameters] Parameter 'foundFreelancersPriority' not found.")
      foundFreelancersPriority})
    jobsFoundBySearchPriority = p.getOrElse("jobsFoundBySearchPriority", {
      logger.worn("[Worker.setParameters] Parameter 'jobsFoundBySearchPriority' not found.")
      jobsFoundBySearchPriority})
    jobsFoundByAnalisePriority = p.getOrElse("jobsFoundByAnalisePriority", {
      logger.worn("[Worker.setParameters] Parameter 'jobsFoundByAnalisePriority' not found.")
      jobsFoundByAnalisePriority})
    toTrackingJobPriority = p.getOrElse("toTrackingJobPriority", {
      logger.worn("[Worker.setParameters] Parameter 'toTrackingJobPriority' not found.")
      toTrackingJobPriority})
    searchNewJobTimeout = p.getOrElse("searchNewJobTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'searchNewJobTimeout' not found.")
      searchNewJobTimeout})
    buildJobsScrapingTaskTimeout = p.getOrElse("buildJobsScrapingTaskTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'buildJobsScrapingTaskTimeout' not found.")
      buildJobsScrapingTaskTimeout})
    nextJobCheckTimeout = p.getOrElse("nextJobCheckTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'nextJobCheckTimeout' not found.")
      nextJobCheckTimeout})
    maxNumberOfCheckedJob = p.getOrElse("maxNumberOfCheckedJob", {
      logger.worn("[Worker.setParameters] Parameter 'maxNumberOfCheckedJob' not found.")
      maxNumberOfCheckedJob})
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
  def getMetrics:(Int,Int,Int,Int,Double,Int) = {
    (queueSize,numberOfProcJob,numberOfFoundJob,numberOfFoundJobInDB,lastParsingQuality,numberOfFailureParsed)}}

