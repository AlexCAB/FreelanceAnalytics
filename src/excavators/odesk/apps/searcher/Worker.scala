package excavators.odesk.apps.searcher

import java.io.File
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.parsers.HTMLJobParsers
import util.parameters.ParametersMap
import util.tasks.{TimedTaskExecutor, TimedTask}
import excavators.odesk.ui.{ManagedWorker, Browser}
import util.logging.Logger
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
  private var jobsFoundBySearchExcavatorNumber = 1
  private var searchNewJobTimeout = 1000 * 60 * 50
  private var excavatorsManagementTimeout = 1000 * 10
  private var maxNumberOfCheckedJob = 30
  //Helpers
  private val htmlParser = new HTMLJobParsers
  //Variables
  private var numberOfFoundJob = 0
  //Construction
  super.setPaused(true)
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
      priority = jobsFoundBySearchExcavatorNumber,
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
  private def getJobExWorkStateDataAndLockParams:Option[(Map[Int,Int],Map[Int,(Boolean,Double)])] = {
    val d = try{
      Some((db.countJobsFoundByToScrapPriority, db.getJobExcavatorsStateParam(setLock = true)))}
    catch{case e:Exception => {
      logger.error("[Worker.getJobExWorkStateDataAndLockParams] Exception on DB access: " + e)
      None}}
    d.map{case (fqs,(es,pls)) => {
      if(pls){logger.worn("[Worker.getJobExWorkStateDataAndLockParams] Parameters been locked.")}
      (fqs,es)}}}
  private def updateJobExAndUnlockWorkStateParameter(es:Map[Int,(Boolean,Double)]) = {
    try{
      db.updateJobExcavatorsStateParam(es, setLock = false)}
    catch{case e:Exception => {
      logger.error("[Worker.updateJobExAndUnlockWorkStateParameter] Exception on DB access: " + e)}}}
  private def getFreelancerExWorkStateDataAndLockParams:Option[(Map[Int,Int],Map[Int,(Boolean,Double)])] = {
    val d = try{
      Some((db.countFreelancersFoundByToScrapPriority, db.getFreelancerExcavatorsStateParam(setLock = true)))}
    catch{case e:Exception => {
      logger.error("[Worker.getFreelancerExWorkStateDataAndLockParams] Exception on DB access: " + e)
      None}}
    d.map{case (fqs,(es,pls)) => {
      if(pls){logger.worn("[Worker.getJobExWorkStateDataAndLockParams] Parameters been locked.")}
      (fqs,es)}}}
  private def updateFreelancerExAndUnlockWorkStateParameter(es:Map[Int,(Boolean,Double)]) = {
    try{
      db.updateFreelancerExcavatorsStateParam(es, setLock = false)}
    catch{case e:Exception => {
      logger.error("[Worker.updateFreelancerExAndUnlockWorkStateParameter] Exception on DB access: " + e)}}}
  private def calcDistribution(eqs:List[(Int,Int)]):List[(Int,Double)] = { //Take: L(Excavator, N jobs), Return: (Excavator, Distribution probability)
    val ps = if(eqs.size > 1){ //If several excavators
      val mqs = eqs.maxBy(_._2)._2  //Max queue size
        if(mqs != 0){  //If queues not empty
        val t = eqs.map{case(e,n) => {
            val x = (mqs - n).toDouble
            (e,if(x == 0){x + 1}else if(x == mqs){x - 1}else{x})}}
          val s = t.map(_._2).sum
          t.map{case(e,n) => (e,(n / s))}}
        else{ //If no jobs in queue then peer distribution
        val p = 1.0 / eqs.size
          eqs.map{case(e,_) => (e,p)}}}
      else if(eqs.size == 1){ //If one excavator
        eqs.map{case(e,n) => (e,1.0)}} //All work to it
      else{ //No excavators
        List()}
      ps}
  private def distributeFoundJobsToWorkingExcavators(ses:List[Int], es:Map[Int,(Boolean,Double)]) = if(ses.nonEmpty){
    try{
      db.redistributeFoundJobsFromToWithProb(ses,es.map{case(e,(_,p)) => (e,p)}.toList)
      logger.info("[Worker.distributeFoundJobsToWorkingExcavators] Fount jobs distribute form: " + ses + ", to: " + es)}
    catch{case e:Exception => {
      logger.error("[Worker.distributeFoundJobsToWorkingExcavators] Exception on DB access: " + e)}}}
  private def distributeFoundFreelancersToWorkingExcavators(ses:List[Int], es:Map[Int,(Boolean,Double)]) = if(ses.nonEmpty){
    try{
      db.redistributeFoundFreelancersFromToWithProb(ses,es.map{case(e,(_,p)) => (e,p)}.toList)
      logger.info("[Worker.distributeFoundFreelancersToWorkingExcavators] Fount freelancers distribute form: " + ses + ", to: " + es)}
    catch{case e:Exception => {
      logger.error("[Worker.distributeFoundFreelancersToWorkingExcavators] Exception on DB access: " + e)}}}
  //Tasks
  case class SearchNewJobsTask(t:Long) extends TimedTask(t, searchNewJobsTaskPriority){def execute() = {
    //Log
    logger.info("[Worker.SearchNewJobs] Start search of new jom.")
    //Search works
    val (nf,nc) = searchAndSaveJobs()
    //Add next task
    val mt = searchNewJobTimeout / 5
    val nt = ((searchNewJobTimeout - mt) * random).toInt + mt
    numberOfFoundJob += nc
    logger.info("[Worker.SearchNewJobs] End searching of new job, next via " + (nt / 1000) +
      " sec., found " + nf + ", collected " + nc + " jobs.")
    addTask(new SearchNewJobsTask(nt + System.currentTimeMillis()))}}
  case class ExcavatorsManagementTask(t:Long) extends TimedTask(t, searchNewJobsTaskPriority){def execute() = {
    //Get job excavators work state data
    getJobExWorkStateDataAndLockParams match{
      case Some((fqs,es)) => {
        logger.info("[Worker.ExcavatorsManagementTask] Jobs excavators load: " + fqs)
        //Calc priority
        val wes = es.filter{case(n,(true,_)) if(n != jobsFoundBySearchExcavatorNumber) => true; case _ => false}.toList //Working excavators
        val eqs = wes.map{case(e,(true,_)) => (e, if(fqs.contains(e)){fqs(e)}else{0})} //Active excavators to they queue size
        val ps = calcDistribution(eqs)
        val nes = ps.map{case(e,p) => (e,(true,p))}.toMap
        //Distribute found jobs from end work excavators to working if is
        if(nes.nonEmpty) {
          val les = fqs.toList.map(_._1).filter(e => {(e != jobsFoundBySearchExcavatorNumber)&&(! nes.contains(e))}) //All not working excavators
          distributeFoundJobsToWorkingExcavators(les, nes)}
        else {
          logger.worn("[Worker.ExcavatorsManagementTask] No working jobs excavators.")}
        //Get and check first excavator
        val aes = es.find{case(n,_) => {n == jobsFoundBySearchExcavatorNumber}} match{
          case Some((e,(true,_))) => {nes + (e -> (true,0.0))}
          case _ => {
            logger.worn("[Worker.ExcavatorsManagementTask] No found by search job (first) jobs excavator.")}
            nes}
        //Update params
        updateJobExAndUnlockWorkStateParameter(aes)
        logger.info("[Worker.ExcavatorsManagementTask] Job excavator work state parameters updated: " + aes)}
      case None =>}
    //Get freelancers excavators work state data
    getFreelancerExWorkStateDataAndLockParams match{
      case Some((fqs,es)) => {
        logger.info("[Worker.ExcavatorsManagementTask] Freelancers excavators load: " + fqs)
        //Calc priority
        val wes = es.filter{case(_,(s,_)) => s}.toList //Working excavators
        val eqs = wes.map{case(e,(true,_)) => (e, if(fqs.contains(e)){fqs(e)}else{0})} //Active excavators to they queue size
        val ps = calcDistribution(eqs)
        val nes = ps.map{case(e,p) => (e,(true,p))}.toMap
        //Distribute
        if(nes.nonEmpty) {
          val les = fqs.toList.map(_._1).filter(e => ! nes.contains(e)) //All not working excavators
          distributeFoundFreelancersToWorkingExcavators(les, nes)}
        else {
          logger.worn("[Worker.ExcavatorsManagementTask] No working freelancers excavators.")}
        //Update params
        updateFreelancerExAndUnlockWorkStateParameter(nes)
        logger.info("[Worker.ExcavatorsManagementTask] Freelancers excavator work state parameters updated: " + nes)}
      case None =>}
    //Add next task
    addTask(new ExcavatorsManagementTask(excavatorsManagementTimeout + System.currentTimeMillis()))}}
  //Methods
  def setParameters(p:ParametersMap) = {
    jobSearchURL = p.getOrElse("jobSearchURL", {
      logger.worn("[Worker.setParameters] Parameter 'jobSearchURL' not found.")
      jobSearchURL})
    jobsFoundBySearchExcavatorNumber = p.getOrElse("jobsFoundBySearchExcavatorNumber", {
      logger.worn("[Worker.setParameters] Parameter 'jobsFoundBySearchExcavatorNumber' not found.")
      jobsFoundBySearchExcavatorNumber})
    searchNewJobTimeout = p.getOrElse("searchNewJobTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'searchNewJobTimeout' not found.")
      searchNewJobTimeout})
    maxNumberOfCheckedJob = p.getOrElse("maxNumberOfCheckedJob", {
      logger.worn("[Worker.setParameters] Parameter 'maxNumberOfCheckedJob' not found.")
      maxNumberOfCheckedJob})
    excavatorsManagementTimeout = p.getOrElse("excavatorsManagementTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'excavatorsManagementTimeout' not found.")
      excavatorsManagementTimeout})}
  def init() = {
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

