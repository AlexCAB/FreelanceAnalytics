package excavators.odesk.ex_jods

import java.awt.image.BufferedImage
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.DBProvider
import excavators.odesk.structures._
import excavators.util.tasks.{TaskExecutor,Task}
import java.io.File
import excavators.odesk.parsers.HTMLParsers
import excavators.odesk.ui.{ManagedWorker, Browser}
import excavators.util.logging.Logger
import scala.math.random

/**
 * Class encapsulate work logic
 * Created by CAB on 13.10.2014.
 */

class Worker(browser:Browser, logger:Logger, db:DBProvider) extends ManagedWorker with TaskExecutor{
  //Parameters
  val runAfterStart = false
  val searchNewJobTimeout = 1000 * 2000 //1000 * 60 * 50
  val buildJobsScrapingTaskTimeout = 1000 * 60 * 30
  val numberOfJobToScripInIteration = 200
  val maxNumberOfCheckedJob = 10
  val overloadFoundJobTableRowNumber = 100000
  val foundFreelancersPriority = 1
  val mainPageURL = "https://www.odesk.com"
  val jobSearchURL = "https://www.odesk.com/jobs/?q="
  val htmlSaveFolder = System.getProperty("user.home") + "\\Desktop"
  val collectJobsTaskPriority = 3
  val buildJobsScrapingTaskPriority = 4
  val jobsFoundBySearchScrapTaskPriority = 2
  val jobsFoundByAnaliseScrapTaskPriority = 1
  val jobsFoundBySearchPriority = 1
  val jobsFoundByAnalisePriority = 1
  val toTrackingJobPriority = 1
  val sizeOfSetLastJobs = 100
  val logoImageCoordinates = (0,0,100,100) //x,y,w,h
  val wornParsingQualityLevel = 0.8
  val errorParsingQualityLevel = 0.5

  //Construction
  super.setPaused(! runAfterStart)
  addTask(new BuildJobsScrapingTask(System.currentTimeMillis()))
  addTask(new CollectJobs(System.currentTimeMillis()))
  val htmlParser = new HTMLParsers
  //Functions
  private def saveFoundJobsToDB(fj:List[FoundWork], ljs:Set[String]):(Int,Int) = { //Return (N saved, N in 'ljs')
    //Preparing jobs to save
    val pj = fj.filter(j => {! ljs.contains(j.url)}).map(j => FoundJobsRow(
      id = 0,
      oUrl = j.url,
      foundBy = FoundBy.Search,
      date = new Date,
      priority = jobsFoundBySearchPriority,
      skills = j.skills,
      nFreelancers = j.nFreelancers))
    //Save jobs to DB
    val na = pj.map(j => {
      try{
        db.addFoundJobsRow(j)
        1}
      catch{case e:Exception => {
        logger.error("[Worker.saveFoundJobsToDB] Job " + j.oUrl + " not added: " + e)
        0}}})
    //Return n added
    (na.sum, (fj.size - pj.size))}
  private def getFreelancerIdByUrl(oUrl:Option[String]):Option[Long] = {
    oUrl.flatMap(url => {
      try{
        db.getFreelancerIdByURL(url)}
      catch{case e:Exception => {
        logger.error("[Worker.getFreelancerIdByUrl] Exception: " + e)
        None}}})}
  private def getImageByUrl(oUrl:Option[String], cut:(Int,Int,Int,Int)):Option[BufferedImage] = {
    oUrl.flatMap(url => {
      browser.getHTMLbyURL(url) match{
        case Some(html) if(html != "") => {
          Some(browser.captureImage.getSubimage(cut._1,cut._2,cut._3,cut._4))}
        case _ => {
          logger.worn("[Worker.getImageByUrl] Failure on get image: " + url)
          None}}})}
  private def getSetOfLastJobs:Set[String] = {
    try{
      db.getSetOfLastJobsURLoFundBy(sizeOfSetLastJobs, FoundBy.Search)}
    catch{case e:Exception => {
      logger.error("[Worker.getSetOfLastJobs] Exception on getSetOfLastJobsURL: " + e)
      Set[String]()}}}
  private def searchAdnSaveJobs(ljs:Set[String]):(Int,Int) = { //Return: (number of found, number of collected)
    browser.getHTMLbyURL(jobSearchURL) match{
      case Some(fHtml) if(fHtml != "") => {
        //Parse search result HTML
        val fsr = htmlParser.parseWorkSearchResult(fHtml)
        if(fsr.works.nonEmpty){
          var (na, nk) = saveFoundJobsToDB(fsr.works, ljs)
          logger.info("[Worker.searchAdnSaveJobs] On start collect " + na + " jobs.")
          //Collect next to known or max N work
          var nUrl = fsr.nextUrl
          var nChk = fsr.works.size
          while(nk == 0 && nChk < maxNumberOfCheckedJob && nUrl.nonEmpty){
            browser.getHTMLbyURL(mainPageURL + nUrl.get) match {
              case Some(html) if (html != "") => {
                val sr = htmlParser.parseWorkSearchResult(html)
                if(sr.works.nonEmpty){
                  nChk += sr.works.size
                  val (nna, nnk) = saveFoundJobsToDB(sr.works, ljs)
                  na += nna
                  nk = nnk
                  logger.info("[Worker.searchAdnSaveJobs] On next collect " + nna + " jobs, total " + na + "jobs.")}
                else{
                  logger.worn("[Worker.searchAdnSaveJobs] No job in next search result(parse error), at: " + nUrl)}
                nUrl = sr.nextUrl}
              case _ => {
                logger.worn("[Worker.searchAdnSaveJobs] No next job search result at: " + nUrl)
                nUrl = None}}}
          if(nChk >= maxNumberOfCheckedJob){
            logger.worn("[Worker.searchAdnSaveJobs] More jobs then max (" + maxNumberOfCheckedJob + ")")}
          //Return N found and N added
          (fsr.nFound.getOrElse(0), na)}
        else{
          logger.worn("[Worker.searchAdnSaveJobs] No job in search result(parse error).")
          (0,0)}}
      case _ => {
        logger.worn("[Worker.searchAdnSaveJobs] No job search result.")
        (0,0)}}}
  private def getFoundJobs(n:Int, fb:FoundBy):(List[FoundJobsRow],  Int) = {
    try{
      db.getNOfOldFoundByJobs(n, fb)}
    catch{case e:Exception => {
      logger.error("[Worker.getFoundJobs] Exception on getNOfOldFoundJobs: " + e)
      (List[FoundJobsRow](),0)}}}
  private def checkIfJojAlreadyScraped(url:String):(Boolean,Boolean) = { //(check successful, job is scraped)
    try{
      (true, db.isJobScraped(url))}
    catch{case e:Exception => {
      logger.error("[Worker.checkIfJojAlreadyScraped] Exception on isJobScraped: " + e)
      (false,false)}}}
  private def addToTrackingTable(url:String) = {
    try{
      db.addJobToTrackingRow(ToTrackingJob(
        id = 0,
        oUrl = url,
        date = new Date,
        priority = toTrackingJobPriority))}
    catch{case e:Exception => {
      logger.error("[Worker.addToTrackingTable] Exception on addJobToTrackingRow: " + e)}}}
  private def getAndParseJob(url:String):(Option[ParsedJob],Option[String]) = {
    browser.getHTMLbyURL(mainPageURL + url) match{
      case Some(html) if(html != "") => {
        (htmlParser.parseJob(html), Some(html))}
      case _ => {
        logger.worn("[Worker.getAndParseJob] No job html on: " + url)
        (None,None)}}}
  private def estimateParsingQuality(pj:Option[ParsedJob]):Double = {
???
    1.0
  }
  private def saveWrongParsedHtml(url:String, html:Option[String], pq:Double) = {
???

  }
  private def saveMainJobData(j:FoundJobsRow, opj:Option[ParsedJob]):Option[Long] = opj.flatMap(pj => {
      val jr = JobsRow(
        id = 0,
        foundData = j,
        daeDate = None,
        deleteDate = None,
        nextCheckDate = None,
        jabData = pj.job)
      try{
        val id = db.addJobsRow(jr)
        logger.info("[Worker.JobsScraping] Added job: " + jr.foundData.oUrl + ", id = " + id)
        Some(id)}
      catch{case e:Exception => {
        logger.error("[Worker.JobsScraping] Exception on save job: " + e + ", data=" + jr)
        None}}})
  private def prepareJobDataToSave(pj:Option[ParsedJob], jid:Option[Long]):
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
        logo = getImageByUrl(pj.clientChanges.logoUrl, logoImageCoordinates))
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
        val cd = new Date
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
        val cd = new Date
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
  private def saveJobDataToDB(prs:Option[(JobsChangesRow, ClientsChangesRow, List[JobsApplicantsRow], List[JobsHiredRow],
  List[ClientsWorksHistoryRow], Set[FoundFreelancerRow], Set[FoundJobsRow])]):Unit = prs match{
    case Some((jcr,ccr,ars,jhr,whr,ffr,fjr)) => {
      try{
        db.addJobsChangesRow(jcr)
        logger.info("[Worker.JobsScraping] Added job changes, job id = " + jcr.jobId)
        db.addClientsChangesRow(ccr)
        logger.info("[Worker.JobsScraping] Added client changes, job id = " + jcr.jobId)
        ars.foreach(r => db.addJobsApplicantsRow(r))
        logger.info("[Worker.JobsScraping] Added " + ars.size + " applicants, job id = " + jcr.jobId)
        jhr.foreach(r => db.addJobsHiredRow(r))
        logger.info("[Worker.JobsScraping] Added " + jhr.size + " jobs hired, job id = " + jcr.jobId)
        whr.foreach(r => db.addClientsWorksHistoryRow(r))
        logger.info("[Worker.JobsScraping] Added " + whr.size + " clients works history, job id = " + jcr.jobId)
        ffr.foreach(r => db.addFoundFreelancerRow(r))
        logger.info("[Worker.JobsScraping] Added " + ffr.size + " found freelancerR, job id = " + jcr.jobId)
        fjr.foreach(r => db.addFoundJobsRow(r))
        logger.info("[Worker.JobsScraping] Added " + fjr.size + " found jobs, job id = " + jcr.jobId)}
      catch{case e:Exception =>
        logger.error("[Worker.JobsScraping] Exception on save job data: " + e)}}
    case None =>}
  private def delFromFoundJobs(j:FoundJobsRow) = {
    ???

  }
  //Tasks
  case class CollectJobs(t:Long) extends Task(t, collectJobsTaskPriority){def execute() = {
    //Log
    logger.info("[Worker.CollectJobs] Start collection of new jom.")
    //Get set of last jobs
    val ljs = getSetOfLastJobs
    //Search works
    val (nf,nc) = searchAdnSaveJobs(ljs)
    //Add BuildJobsScrapingTask task if added some jobs.
    if(nc != 0){
      addTask(new BuildJobsScrapingTask(System.currentTimeMillis()))}
    //Add next task
    val mt = searchNewJobTimeout / 5
    val nt = ((searchNewJobTimeout - mt) * random).toInt + mt
    logger.info("[Worker.CollectJobs] End collection of new job, next via " + (nt / 1000) +
      " sec., found " + nf + ", collected " + nc + " jobs.")
    addTask(new CollectJobs(nt + System.currentTimeMillis()))}}
  case class BuildJobsScrapingTask(t:Long) extends Task(t, buildJobsScrapingTaskPriority){def execute() = {
    //Get jobs
    val (sfj, n) = getFoundJobs(numberOfJobToScripInIteration, FoundBy.Search)
    if(n > overloadFoundJobTableRowNumber){
      logger.worn("[Worker.BuildJobsScrapingTask] Found more jobs then maxNumberOfJobToScrip, n = " + n)}
    val afj = if(sfj.size < numberOfJobToScripInIteration){ //If in this iteration can be processed jobs found by analyses
      getFoundJobs((numberOfJobToScripInIteration - sfj.size), FoundBy.Analyse)._1}
    else{
      List[FoundJobsRow]()}
    //Add scraping tasks
    val js = sfj.map(j => (j, jobsFoundBySearchScrapTaskPriority)) ++ afj.map(j => (j, jobsFoundByAnaliseScrapTaskPriority))
    val ct = System.currentTimeMillis()
    if(js.nonEmpty){
      val ato = buildJobsScrapingTaskTimeout / js.size
      js.zipWithIndex.foreach{case((j,p),i) => {
        val nrt = ct + (i - 1) * ato + (ato * random).toInt
        addTask(new JobsScraping(nrt,p,j))}}}
    logger.info("[Worker.BuildJobsScrapingTask] Added " + js.size + " jobs to scraping.")
    //Add self to next run
    addTask(new BuildJobsScrapingTask(ct + buildJobsScrapingTaskTimeout))}}
  case class JobsScraping(t:Long, p:Int, j:FoundJobsRow) extends Task(t, p){def execute() = {
    logger.info("[Worker.BuildJobsScrapingTask] Start scrap url: " + j.oUrl)
    //Check if jib already scraped
    val cr = checkIfJojAlreadyScraped(j.oUrl) //(check successful, job is scraped)
    //Select activity
    cr match{
      case (true,true) => { //(job is scraped, check successful)
        //Add to tracking list
        addToTrackingTable(j.oUrl)}
      case (true,false) => { //(job  not scraped, check successful)
        //Get ant parse HTML
        val (pj, html) = getAndParseJob(j.oUrl)
        //Estimate parsing quality
        val pq = estimateParsingQuality(pj)
        if(pq <= errorParsingQualityLevel){
          logger.error("[Worker.BuildJobsScrapingTask] Job parsing error, url: " + j.oUrl)}
        else if(pq <= wornParsingQualityLevel){
          logger.worn("[Worker.BuildJobsScrapingTask] Job parsing worn, url: " + j.oUrl)}
        //Save source HTML if parsing quality low
        if(pq <= errorParsingQualityLevel || pq <= wornParsingQualityLevel){
          saveWrongParsedHtml(j.oUrl, html, pq)}
        //Save job row, get job ID
        val jid = saveMainJobData(j,pj)
        //Prepare job data to save to DB
        val prs = prepareJobDataToSave(pj,jid)
        //Safe rows to DB
        saveJobDataToDB(prs)
        //If job save add to tracking, and remove from found
        jid match{
          case Some(_) => {
            addToTrackingTable(j.oUrl)
            delFromFoundJobs(j)}
          case None =>}}
      case _ =>}}}
  //Methods
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
        val p = htmlSaveFolder + "\\" + System.currentTimeMillis() + ".html"
        try{
          tools.nsc.io.File(p).writeAll(t)
          logger.info("[Worker.saveHtml] File save to: " + p)}
        catch{case e:Exception => {
          logger.error("[Worker.saveHtml] Exception when save: " + e)}}}
      case None => logger.worn("[Worker.saveHtml] Not save, is empty.")}}
  def saveScreenshot() = {
    val img = browser.captureImage
    val p = htmlSaveFolder + "\\" + System.currentTimeMillis() + ".png"
    val f = new File(p)
    ImageIO.write(img, "PNG", f)
    logger.info("[Worker.saveScreenshot] File save to: " + p)}
  override def setPaused(s:Boolean) = {
    super.setPaused(s)
    if(s){
      logger.info("[Worker.setWork] Paused.")}
    else{
      logger.info("[Worker.setWork] Run.")}}}
