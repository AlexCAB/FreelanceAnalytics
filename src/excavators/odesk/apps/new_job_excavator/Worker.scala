package excavators.odesk.apps.new_job_excavator

import java.util.Date
import excavators.odesk.apps.AbstractWorker
import excavators.odesk.db.{ODeskExcavatorsDBProvider, Saver}
import excavators.odesk.structures._
import excavators.util.parameters.ParametersMap
import excavators.util.tasks.TimedTask
import excavators.odesk.ui.Browser
import excavators.util.logging.Logger
import scala.math.random

/**
* Class encapsulate work logic
* Created by CAB on 13.10.2014.
*/

class Worker(browser:Browser, logger:Logger, saver:Saver, db:ODeskExcavatorsDBProvider) extends AbstractWorker(browser,logger,db){
  //Parameters
  private var jobSearchURL = "https://www.odesk.com/jobs/?q="
  private val searchNewJobsTaskPriority = 2
  private val jobsFoundBySearchScrapTaskPriority = 1
  private var foundFreelancersPriority = 100
  private var jobsFoundBySearchPriority = 100
  private var jobsFoundByAnalisePriority = 100
  private var toTrackingJobPriority = 1
  private var searchNewJobTimeout = 1000 * 60 * 50
  private var nextJobCheckTimeout = 1000 * 60 * 60
  private var maxNumberOfCheckedJob = 100
  private val waitSaverTimeout = 1000 * 10
  //Variables
  private var numberOfFoundJob = 0
  private var numberOfFoundJobInDB = 0
  //Construction
  super.setPaused(! runAfterStart)
  addTask(new BuildJobsScrapingTask(System.currentTimeMillis() + buildJobsScrapingTaskTimeout, true))
  addTask(new SearchNewJobs(System.currentTimeMillis()))
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
    val na = try{
      db.addFoundJobsRows(pj)}
    catch{case e:Exception => {
      logger.error("[Worker.saveFoundJobsToDB] exception: " + e)
      0}}
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
  //Tasks
  case class SearchNewJobs(t:Long) extends TimedTask(t, searchNewJobsTaskPriority){def execute() = {
    //Log
    logger.info("[Worker.SearchNewJobs] Start search of new jom.")
    //Search works
    val (nf,nc) = searchAndSaveJobs()
    //Add BuildJobsScrapingTask task if added some jobs.
    addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + waitSaverTimeout), false))
    //Add next task
    val mt = searchNewJobTimeout / 5
    val nt = ((searchNewJobTimeout - mt) * random).toInt + mt
    numberOfFoundJob += nc
    logger.info("[Worker.SearchNewJobs] End searching of new job, next via " + (nt / 1000) +
      " sec., found " + nf + ", collected " + nc + " jobs.")
    addTask(new SearchNewJobs(nt + System.currentTimeMillis()))}}
  case class BuildJobsScrapingTask(t:Long, isTimed:Boolean) extends TimedTask(t, buildJobsScrapingTaskPriority){def execute() = {
    //I sever not end work, then wait
    if(saver.isInProcess){
      addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + waitSaverTimeout), false))
      logger.worn("[Worker.BuildJobsScrapingTask] Saver is to slow.")}
    else{
      //Get jobs
      val (js, n) = getFoundJobs(numberOfJobToScripInIteration, FoundBy.Search)
      numberOfFoundJobInDB = n
      //Add scraping tasks
      js.foreach(j => {addTask(new JobsScraping(0,j,1))})
      //Logging
      logger.info("[Worker.BuildJobsScrapingTask] " + js.size + " new jobs added to scraping.")}
      //Add self to task set
      if(isTimed){
        addTask(new BuildJobsScrapingTask(System.currentTimeMillis() + buildJobsScrapingTaskTimeout, true))}}}
  case class JobsScraping(t:Long, j:FoundJobsRow, nScrapTry:Int) extends TimedTask(t, jobsFoundBySearchScrapTaskPriority){def execute() = {
    //Start
    logger.info("[Worker.BuildJobsScrapingTask] Start scrap,\n  url: " + j.oUrl)
    numberOfProcJob += 1
    //Add build jobs if no more in queue
    if(getNumTaskLike(this) == 0){addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + waitSaverTimeout), false))}
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
  def getMetrics:(Int,Int,Int,Int,Double,Int) = {
    (queueSize,numberOfProcJob,numberOfFoundJob,numberOfFoundJobInDB,lastParsingQuality,numberOfFailureParsed)}}

