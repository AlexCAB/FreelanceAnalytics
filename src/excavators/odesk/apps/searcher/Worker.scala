package excavators.odesk.apps.searcher

import java.io.File
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.parsers.HTMLJobParsers
import excavators.util.parameters.ParametersMap
import excavators.util.tasks.{TimedTaskExecutor, TimedTask}
import excavators.odesk.ui.{ManagedWorker, Browser}
import excavators.util.logging.Logger
import util.structures.{FoundWork, FoundJobsRow, FoundBy}
import scala.math.random

/**
* Class encapsulate work logic
* Created by CAB on 13.10.2014.
*/

class Worker(browser:Browser, logger:Logger, db:ODeskExcavatorsDBProvider) extends ManagedWorker with TimedTaskExecutor{
  //Parameters
  private val mainPageURL = "https://www.odesk.com"
  private var jobSearchURL = "https://www.odesk.com/jobs/?q="
  private val saveFolder = System.getProperty("user.home") + "\\Desktop"
  private val searchNewJobsTaskPriority = 2
 // private val jobsFoundBySearchScrapTaskPriority = 1
//  private var foundFreelancersPriority = 100
  private var jobsFoundBySearchExcavatorNumber = 1
 // private val jobsFoundByAnalisePriority = 1
//  private var toTrackingJobPriority = 1
  private var searchNewJobTimeout = 1000 * 60 * 50
  private var excavatorsManagementTimeout = 1000 * 10
//  private var nextJobCheckTimeout = 1000 * 60 * 60
  private var maxNumberOfCheckedJob = 30
 // private val waitSaverTimeout = 1000 * 10
  //Helpers
  private val htmlParser = new HTMLJobParsers
  //Variables
  private var runAfterStart = false
  private var numberOfFoundJob = 0
 // private var numberOfFoundJobInDB = 0
  //Construction
  super.setPaused(! runAfterStart)
//  addTask(new BuildJobsScrapingTask(System.currentTimeMillis() + buildJobsScrapingTaskTimeout, true))
  addTask(new SearchNewJobsTask(0))
  addTask(new ExcavatorsManagementTask(0))
  //Functions
  private def saveFoundJobsToDB(fj:List[FoundWork]):(Int,Boolean) = { //Return (N saved, Has already exist in DB)
    //Preparing jobs to save
    val pj = fj.map(j => FoundJobsRow(
      id = 0,
      oUrl = j.url,
      foundBy = FoundBy.Search,
      date = new Date,
      priority = ((scala.math.random * 5) + 1).toInt,//jobsFoundBySearchExcavatorNumber,        <--- !!!!!!!
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
              case Some(html) if(html != "") => {
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
  private def getWorkStateDataAndLockParams:Option[(Map[Int,Int],Map[Int,(Boolean,Double)])] = {
    val d = try{
      Some((db.countFoundByToScrapPriority, db.getExcavatorsStateParam(setLock = true)))}
    catch{case e:Exception => {
      logger.error("[Worker.getWorkStateDataAndLockParams] Exception on DB access: " + e)
      None}}
    d.map{case (fqs,(es,pls)) => {
      if(pls){logger.worn("[Worker.getWorkStateDataAndLockParams] Parameters been locked.")}
      (fqs,es)}}}
  private def updateAndUnlockWorkStateParameter(es:Map[Int,(Boolean,Double)]) = {
    try{
      db.updateExcavatorsStateParam(es, setLock = false)}
    catch{case e:Exception => {
      logger.error("[Worker.updateAndUnlockWorkStateParameter] Exception on DB access: " + e)}}}
  private def distributeFoundJobsToWorkingExcavators(ses:List[Int], es:Map[Int,(Boolean,Double)]) = {
    logger.info("[Worker.distributeFoundJobsToWorkingExcavators] Fount jobs distribute form: " + ses + ", to: " + es)






  }




  //Tasks
  case class SearchNewJobsTask(t:Long) extends TimedTask(t, searchNewJobsTaskPriority){def execute() = {
    //Log
    logger.info("[Worker.SearchNewJobs] Start search of new jom.")
    //Search works
    val (nf,nc) = searchAndSaveJobs()
//    //Add BuildJobsScrapingTask task if added some jobs.
//    addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + waitSaverTimeout), false))
    //Add next task
    val mt = searchNewJobTimeout / 5
    val nt = ((searchNewJobTimeout - mt) * random).toInt + mt
    numberOfFoundJob += nc
    logger.info("[Worker.SearchNewJobs] End searching of new job, next via " + (nt / 1000) +
      " sec., found " + nf + ", collected " + nc + " jobs.")
    addTask(new SearchNewJobsTask(nt + System.currentTimeMillis()))}}
  case class ExcavatorsManagementTask(t:Long) extends TimedTask(t, searchNewJobsTaskPriority){def execute() = {
    println("ExcavatorsManagementTask")
    //Get work state data
    getWorkStateDataAndLockParams match{
      case Some((fqs,es)) => {
        println(fqs,es)
        //Calc priority
        val wes = es.filter{case(n,(true,_)) if(n != jobsFoundBySearchExcavatorNumber) => true; case _ => false} //Working excavators
        //If is working excavators redistribute priorities, do nothing else
        val nes = if(wes.size != 0){
          val nes = if(wes.nonEmpty){
            val njs = wes.map{case(e,(true,_)) => (e, if(fqs.contains(e)){fqs(e)}else{0})} //Active excavators to they queue size



            val nj = njs.map(_._2).sum //Total job
            val ps = if(nj != 0){
              if(njs.size == 1){
                njs.map{case(e,n) => (e,1.0)}} //If one active, all work to it
              else{
                val mnj = njs.maxBy(_._2)._2  //Max queue size
                println(mnj,njs,nj)
                njs.map{case(e,n) => (e,(mnj - n).toDouble / nj)}}} //Calc found job adding probability
            else{
                val p = 1.0 / wes.size
                njs.map{case(e,_) => (e,p)}}



            println(ps, ps.map(_._2).sum)
            ps.map{case(e,p) => (e,(true,p))}.toMap}
          else{
            Map[Int,(Boolean,Double)]()}
          println(nes)
          //Distribute found jobs from end work excavators to working
          val les = fqs.toList.map(_._1).filter(e => {(e != jobsFoundBySearchExcavatorNumber)&&(! nes.contains(e))}) //All not working excavators
          println(les)
          distributeFoundJobsToWorkingExcavators(les, nes)
          nes}
        else{
          logger.worn("[Worker.ExcavatorsManagementTask] No working excavators.")
          Map[Int,(Boolean,Double)]()}
        //Get and check first excavator
        val aes = es.find{case(n,_) => {n == jobsFoundBySearchExcavatorNumber}} match{
          case Some((e,(true,_))) => {nes + (e -> (true,0.0))}
          case _ => {
            logger.worn("[Worker.ExcavatorsManagementTask] No found by search job (first) excavator.")}
            nes}
        println("aes = " + aes)
        //Update params
        updateAndUnlockWorkStateParameter(aes)
        logger.info("[Worker.ExcavatorsManagementTask] Excavator work state parameters updated: " + aes)}
      case None =>}














    //Add next task
    addTask(new ExcavatorsManagementTask(excavatorsManagementTimeout + System.currentTimeMillis()))}}



//  case class BuildJobsScrapingTask(t:Long, isTimed:Boolean) extends TimedTask(t, buildJobsScrapingTaskPriority){def execute() = {
//    //I sever not end work, then wait
//    if(saver.isInProcess){
//      addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + waitSaverTimeout), false))
//      logger.worn("[Worker.BuildJobsScrapingTask] Saver is to slow.")}
//    else{
//      //Get jobs
//      val (js, n) = getFoundJobs(numberOfJobToScripInIteration, FoundBy.Search)
//      numberOfFoundJobInDB = n
//      //Add scraping tasks
//      js.foreach(j => {addTask(new JobsScraping(0,j,1))})
//      //Logging
//      logger.info("[Worker.BuildJobsScrapingTask] " + js.size + " new jobs added to scraping.")}
//      //Add self to task set
//      if(isTimed){
//        addTask(new BuildJobsScrapingTask(System.currentTimeMillis() + buildJobsScrapingTaskTimeout, true))}}}
//  case class JobsScraping(t:Long, j:FoundJobsRow, nScrapTry:Int) extends TimedTask(t, jobsFoundBySearchScrapTaskPriority){def execute() = {
//    //Start
//    logger.info("[Worker.BuildJobsScrapingTask] Start scrap,\n  url: " + j.oUrl)
//    numberOfProcJob += 1
//    //Add build jobs if no more in queue
//    if(getNumTaskLike(this) == 0){addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + waitSaverTimeout), false))}
//    //Get and parse HTML
//    val (pj, html) = getAndParseJob(j.oUrl)
//    //Date to be use as 'created date'
//    val cd = pj match{case Some(j) => j.job.createDate; case None => new Date}
//    //Estimate parsing quality
//    val pq = estimateQuality(pj,j,html,cd)
//    //If job parsed good enough to save
//    if(pj.nonEmpty && pq > htmlParser.notSaveParsingQualityLevel){
//      //Get logo
//      val l = getImageByUrl(pj.get.clientChanges.logoUrl, htmlParser.logoImageCoordinates)
//      //Add saver task
//      saver.addSaveJobDataAndDelFoundTask(j, pj.get, pq, cd, l)}
//    else{
//      //If parse wrong, add to next time (if no max try number)
//      if(nScrapTry <= scrapTryMaxNumber){
//        //Move pars task to future
//        addTask(new JobsScraping((System.currentTimeMillis() + scrapTryTimeout), j, (nScrapTry + 1)))}
//      else{
//        //If max try worn and remove found job
//        logger.worn("[Worker.JobsScraping] Failure with max scrape try (" + scrapTryMaxNumber + ").")
//        saver.addDelFoundJobTask(j)}}}}
  //Methods
  def setParameters(p:ParametersMap) = {
//    runAfterStart = p.getOrElse("runAfterStart", {
//      logger.worn("[Worker.setParameters] Parameter 'runAfterStart' not found.")
//      runAfterStart})
//    jobSearchURL = p.getOrElse("jobSearchURL", {
//      logger.worn("[Worker.setParameters] Parameter 'jobSearchURL' not found.")
//      jobSearchURL})
//    foundFreelancersPriority = p.getOrElse("foundFreelancersPriority", {
//      logger.worn("[Worker.setParameters] Parameter 'foundFreelancersPriority' not found.")
//      foundFreelancersPriority})
//    jobsFoundBySearchExcavatorNumber = p.getOrElse("jobsFoundBySearchPriority", {
//      logger.worn("[Worker.setParameters] Parameter 'jobsFoundBySearchPriority' not found.")
//      jobsFoundBySearchExcavatorNumber})
//    jobsFoundByAnalisePriority = p.getOrElse("jobsFoundByAnalisePriority", {
//      logger.worn("[Worker.setParameters] Parameter 'jobsFoundByAnalisePriority' not found.")
//      jobsFoundByAnalisePriority})
//    toTrackingJobPriority = p.getOrElse("toTrackingJobPriority", {
//      logger.worn("[Worker.setParameters] Parameter 'toTrackingJobPriority' not found.")
//      toTrackingJobPriority})
//    searchNewJobTimeout = p.getOrElse("searchNewJobTimeout", {
//      logger.worn("[Worker.setParameters] Parameter 'searchNewJobTimeout' not found.")
//      searchNewJobTimeout})
//    buildJobsScrapingTaskTimeout = p.getOrElse("buildJobsScrapingTaskTimeout", {
//      logger.worn("[Worker.setParameters] Parameter 'buildJobsScrapingTaskTimeout' not found.")
//      buildJobsScrapingTaskTimeout})
//    nextJobCheckTimeout = p.getOrElse("nextJobCheckTimeout", {
//      logger.worn("[Worker.setParameters] Parameter 'nextJobCheckTimeout' not found.")
//      nextJobCheckTimeout})
//    maxNumberOfCheckedJob = p.getOrElse("maxNumberOfCheckedJob", {
//      logger.worn("[Worker.setParameters] Parameter 'maxNumberOfCheckedJob' not found.")
//      maxNumberOfCheckedJob})
//    scrapTryMaxNumber = p.getOrElse("scrapTryMaxNumber", {
//      logger.worn("[Worker.setParameters] Parameter 'scrapTryMaxNumber' not found.")
//      scrapTryMaxNumber})
//    scrapTryTimeout = p.getOrElse("scrapTryTimeout", {
//      logger.worn("[Worker.setParameters] Parameter 'scrapTryTimeout' not found.")
//      scrapTryTimeout})
}
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
  def getMetrics:(Int,Int) = {
    (queueSize,numberOfFoundJob)}}

