package excavators.odesk.ex_jods

import java.awt.image.BufferedImage
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.DBProvider
import excavators.odesk.structures._
import excavators.util.parameters.ParametersMap
import excavators.util.tasks.{TimedTaskExecutor,TimedTask}
import java.io.File
import excavators.odesk.parsers.HTMLParsers
import excavators.odesk.ui.{ManagedWorker, Browser}
import excavators.util.logging.Logger
import scala.math.random

/**
 * Class encapsulate work logic
 * Created by CAB on 13.10.2014.
 */

class Worker(browser:Browser, logger:Logger, saver:Saver, db:DBProvider) extends ManagedWorker with TimedTaskExecutor{
  //Parameters General
  private var runAfterStart = false
  //Parameters URLS
  private val mainPageURL = "https://www.odesk.com"
  private var jobSearchURL = "https://www.odesk.com/jobs/?q="
  private val saveFolder = System.getProperty("user.home") + "\\Desktop"
  //Parameters Priority
  private var foundFreelancersPriority = 1
  private var collectJobsTaskPriority = 3
  private var buildJobsScrapingTaskPriority = 4
  private var jobsFoundBySearchScrapTaskPriority = 2
  private var jobsFoundByAnaliseScrapTaskPriority = 1
  private var jobsFoundBySearchPriority = 1
  private var jobsFoundByAnalisePriority = 1
  private var toTrackingJobPriority = 1
  //Parameters Times
  private var searchNewJobTimeout = 1000 * 2000 //1000 * 60 * 50
  private var buildJobsScrapingTaskTimeout = 1000 * 60 //* 30
  private var nextJobCheckTimeout = 1000 * 60 * 60
  //Parameters Numbers
  private var numberOfJobToScripInIteration = 200
  private var maxNumberOfCheckedJob = 100
  private var overloadFoundJobTableRowNumber = 100000
  //Parameters Sizes
  private val sizeOfSetLastJobs = 100
  private var logoImageCoordinates = List(7,7,108,108) //x,y,w,h
  //Parameters Levels
  private var wornParsingQualityLevel = 0.8
  private var errorParsingQualityLevel = 0.5
  private var notSaveParsingQualityLevel = 0.2
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
  private def getImageByUrl(oUrl:Option[String], cut:List[Int]):Option[BufferedImage] = {
    oUrl.flatMap(url => {
      browser.getHTMLbyURL(url) match{
        case Some(html) if(html != "") => {
          Some(browser.captureImage.getSubimage(cut(0),cut(1),cut(2),cut(3)))}
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
  private def estimateParsingQuality(opj:Option[ParsedJob]):Double = opj match {
    case Some(pj) => {
      var r = 1.0
      if(pj.job.postDate == None){r -= 0.3}
      if(pj.job.deadline == None){r -= 0.05}
      if(pj.job.jobTitle == None){r -= 0.3}
      if(pj.job.jobType == None){r -= 0.2}
      if(pj.job.jobPaymentType == Payment.Unknown){r -= 0.3}
      if(pj.job.jobEmployment == Employment.Unknown && pj.job.jobPaymentType == Payment.Hourly){r -= 0.1}
      if(pj.job.jobPrice == None && pj.job.jobPaymentType == Payment.Budget){r -= 0.3}
      if(pj.job.jobLength == None && pj.job.jobPaymentType == Payment.Hourly){r -= 0.1}
      if(pj.job.jobRequiredLevel == SkillLevel.Unknown && pj.job.jobPaymentType == Payment.Hourly){r -= 0.1}
      if(pj.job.jobDescription == None){r -= 0.3}
      if(pj.jobChanges.nApplicants == None){r -= 0.05}
      if(pj.clientChanges.name == None){r -= 0.05}
      if(pj.clientChanges.paymentMethod == PaymentMethod.Unknown){r -= 0.1}
      if(pj.clientChanges.location == None){r -= 0.05}
      if(r < 0.0){r = 0.0}
      r}
    case None => 0.0}
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
        val id = db.addJobsRow(jr)
        logger.info("[Worker.JobsScraping] Added job: " + jr.foundData.oUrl + ", id = " + id + ", parsing quality = " + pq)
        Some(id)}
      catch{case e:Exception => {
        logger.error("[Worker.JobsScraping] Exception on save job: " + e + ", url=" + j.oUrl)
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
  case class CollectJobs(t:Long) extends TimedTask(t, collectJobsTaskPriority){def execute() = {
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
  case class BuildJobsScrapingTask(t:Long) extends TimedTask(t, buildJobsScrapingTaskPriority){def execute() = {
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
  case class JobsScraping(t:Long, p:Int, j:FoundJobsRow) extends TimedTask(t, p){def execute() = {
    logger.info("[Worker.BuildJobsScrapingTask] Start scrap url: " + j.oUrl)
    //Check if jib already scraped
    val cr = checkIfJojAlreadyScraped(j.oUrl) //(check successful, Option(scraped job id, job available))
    //Select activity
    cr match{
      case (true,Some((id:Long, ja:JobAvailable))) => {
        //Update next tracking time if job available
        if(ja != JobAvailable.No){updateNextCheckTime(id)}
        //Del from found jobs
        saver.addDelFoundJobTask(j)
        logger.info("[Worker.JobsScraping] Job already scraped, url = : " + j.oUrl)}
      case (true,None) => { //(job  not scraped, check successful)
        //Get ant parse HTML
        val (pj, html) = getAndParseJob(j.oUrl)
        //Date to be use as 'created date'
        val cd = pj match{case Some(j) => j.job.createDate; case None => new Date}
        //Estimate parsing quality
        val pq = estimateParsingQuality(pj)
        if(pq <= notSaveParsingQualityLevel){
          logger.error("[Worker.JobsScraping] Job parsing error, job not save, pq = " + pq + ", url: " + j.oUrl)}
        else if(pq <= errorParsingQualityLevel){
          logger.error("[Worker.JobsScraping] Job parsing error, pq = " + pq + ", url: " + j.oUrl)}
        else if(pq <= wornParsingQualityLevel){
          logger.worn("[Worker.JobsScraping] Job parsing worn, pq = " + pq + ", url: " + j.oUrl)}
        //Save source HTML if parsing quality low
        if(errorParsingQualityLevel > pq || wornParsingQualityLevel > pq || notSaveParsingQualityLevel > pq){
          saveWrongParsedHtml(j.oUrl, html, pq, cd)}
        //If job parsed good enough to save
        if(pj.nonEmpty && pq > notSaveParsingQualityLevel){
          //Save job row, get job ID
          val jid = saveMainJobData(j,pj,pq, cd)
          //Remove from found if save successful
          jid.foreach(_ => {
            saver.addDelFoundJobTask(j)})
          //Prepare job data to save to DB
          prepareJobDataToSave(pj,jid, cd).map(prs => {
            //Safe rows to DB and and remove from found
            saver.addSaveJobAdditionalDataAndDelFoundTask(prs)})}}
      case _ =>}}}
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
    collectJobsTaskPriority = p.getOrElse("collectJobsTaskPriority", {
      logger.worn("[Worker.setParameters] Parameter 'collectJobsTaskPriority' not found.")
      collectJobsTaskPriority})
    buildJobsScrapingTaskPriority = p.getOrElse("buildJobsScrapingTaskPriority", {
      logger.worn("[Worker.setParameters] Parameter 'buildJobsScrapingTaskPriority' not found.")
      buildJobsScrapingTaskPriority})
    jobsFoundBySearchScrapTaskPriority = p.getOrElse("jobsFoundBySearchScrapTaskPriority", {
      logger.worn("[Worker.setParameters] Parameter 'jobsFoundBySearchScrapTaskPriority' not found.")
      jobsFoundBySearchScrapTaskPriority})
    jobsFoundByAnaliseScrapTaskPriority = p.getOrElse("jobsFoundByAnaliseScrapTaskPriority", {
      logger.worn("[Worker.setParameters] Parameter 'jobsFoundByAnaliseScrapTaskPriority' not found.")
      jobsFoundByAnaliseScrapTaskPriority})
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
    numberOfJobToScripInIteration = p.getOrElse("numberOfJobToScripInIteration", {
      logger.worn("[Worker.setParameters] Parameter 'numberOfJobToScripInIteration' not found.")
      numberOfJobToScripInIteration})
    maxNumberOfCheckedJob = p.getOrElse("maxNumberOfCheckedJob", {
      logger.worn("[Worker.setParameters] Parameter 'maxNumberOfCheckedJob' not found.")
      maxNumberOfCheckedJob})
    overloadFoundJobTableRowNumber = p.getOrElse("overloadFoundJobTableRowNumber", {
      logger.worn("[Worker.setParameters] Parameter 'overloadFoundJobTableRowNumber' not found.")
      overloadFoundJobTableRowNumber})
    logoImageCoordinates = p.getOrElse("logoImageCoordinates", {
      logger.worn("[Worker.setParameters] Parameter 'logoImageCoordinates' not found.")
      logoImageCoordinates})
    wornParsingQualityLevel = p.getOrElse("wornParsingQualityLevel", {
      logger.worn("[Worker.setParameters] Parameter 'wornParsingQualityLevel' not found.")
      wornParsingQualityLevel})
    errorParsingQualityLevel = p.getOrElse("errorParsingQualityLevel", {
      logger.worn("[Worker.setParameters] Parameter 'errorParsingQualityLevel' not found.")
      errorParsingQualityLevel})
    notSaveParsingQualityLevel = p.getOrElse("notSaveParsingQualityLevel", {
      logger.worn("[Worker.setParameters] Parameter 'notSaveParsingQualityLevel' not found.")
      notSaveParsingQualityLevel})}
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

