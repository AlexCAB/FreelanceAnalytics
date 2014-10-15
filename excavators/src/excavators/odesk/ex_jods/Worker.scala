package excavators.odesk.ex_jods

import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.DBProvider
import excavators.odesk.structures.{FoundWork, FoundJobsRow, FoundBy}
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
  val searchNewJobTimeout = 1000 * 40 //1000 * 60 * 50
  val maxNumberOfCheckedJob = 100
  val mainPageURL = "https://www.odesk.com"
  val jobSearchURL = "https://www.odesk.com/jobs/?q="
  val htmlSaveFolder = System.getProperty("user.home") + "\\Desktop"
  val collectJobsPriority = 3
  val buildJobsScrapingTaskPriority = 2
  val jobsScrapingPriority = 1
  val sizeOfSetLastJobs = 100
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
      priority = 1,
      skills = j.skills,
      nFreelancers = j.nFreelancers))
    //Save jobs to DB
    val na = pj.map(j => {
      try{
        db.addFoundJobsRow(j)
        1}
      catch{case e:Exception => {
        logger.error("[Worker.CollectJobs] Job " + j.oUrl + " not added: " + e)
        0}}})
    //Return n added
    (na.sum, (fj.size - pj.size))}
  //Tasks
  case class CollectJobs(t:Long) extends Task(t, collectJobsPriority){def execute() = {
    //Log
    logger.info("[Worker.CollectJobs] Start collection of new jom.")
    //Get set of last jobs
    val ljs = db.getSetOfLastJobsURL(sizeOfSetLastJobs)
    //Search works
    val (nf,nc) = browser.getHTMLbyURL(jobSearchURL) match{
      case Some(fHtml) if(fHtml != "") => {
        //Parse search result HTML
        val fsr = htmlParser.parseWorkSearchResult(fHtml)
        if(fsr.works.nonEmpty){
          var (na, nk) = saveFoundJobsToDB(fsr.works, ljs)
          logger.info("[Worker.CollectJobs] On start collect " + na + " jobs.")
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
                  logger.info("[Worker.CollectJobs] On next collect " + nna + " jobs, total " + na + "jobs.")}
                else{
                  logger.worn("[Worker.CollectJobs] No job in next search result(parse error), at: " + nUrl)}
                nUrl = sr.nextUrl}
              case _ => {
                logger.worn("[Worker.CollectJobs] No next job search result at: " + nUrl)
                nUrl = None}}}
          if(nChk >= maxNumberOfCheckedJob){
            logger.worn("[Worker.CollectJobs] More jobs then max (" + maxNumberOfCheckedJob + ")")}
          //Return N found and N added
          (fsr.nFound.getOrElse(0), na)}
        else{
          logger.worn("[Worker.CollectJobs] No job in search result(parse error).")
          (0,0)}}
      case _ => {
        logger.worn("[Worker.CollectJobs] No job search result.")
        (0,0)}}
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

    println("BuildJobsScrapingTask")


  }}
  case class JobsScraping(t:Long) extends Task(t, jobsScrapingPriority){def execute() = {


    println("JobsScraping")

  }}
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
