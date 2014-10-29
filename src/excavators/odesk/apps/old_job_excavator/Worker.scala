package excavators.odesk.apps.old_job_excavator

import java.util.Date
import excavators.odesk.apps.AbstractWorker
import excavators.odesk.db.{ODeskExcavatorsDBProvider, Saver}
import excavators.odesk.structures._
import excavators.util.parameters.ParametersMap
import excavators.util.tasks.TimedTask
import excavators.odesk.ui.Browser
import excavators.util.logging.Logger

/**
* Class encapsulate work logic
* Created by CAB on 13.10.2014.
*/

class Worker(b:Browser, logger:Logger, saver:Saver, db:ODeskExcavatorsDBProvider) extends AbstractWorker(b,logger,db){
  //Parameters
  private val waitSaverTimeout = 1000 * 10
  //Construction
  super.setPaused(! runAfterStart)
  addTask(new BuildJobsScrapingTask(System.currentTimeMillis(),true))
  //Tasks
  case class BuildJobsScrapingTask(t:Long, isTimed:Boolean) extends TimedTask(t, buildJobsScrapingTaskPriority){def execute() = {
    //I sever not end work, then wait
    if(saver.isInProcess){
      addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + waitSaverTimeout), false))
      logger.worn("[Worker.BuildJobsScrapingTask] Saver is to slow.")}
    else{
      //Get jobs
      val (js, _) = getFoundJobs(numberOfJobToScripInIteration, FoundBy.Analyse)
      //Add scraping tasks
      js.foreach(j => {addTask(new JobsScraping(0,j,1))})
      //Logging
      logger.info("[Worker.BuildJobsScrapingTask] " + js.size + " new jobs added to scraping.")}
    //Add self to task set
    if(isTimed){
      addTask(new BuildJobsScrapingTask((System.currentTimeMillis() + buildJobsScrapingTaskTimeout), true))}}}
  case class JobsScraping(t:Long, j:FoundJobsRow, nScrapTry:Int) extends TimedTask(t, jobsFoundByAnaliseScrapTaskPriority){def execute() = {
    //Start
    logger.info("[Worker.BuildJobsScrapingTask] Start scrap,\n  url: " + j.oUrl)
    numberOfProcJob += 1
    //Add build jobs if no more in queue
    if(getNumTaskLike(this) == 0){addTask(new BuildJobsScrapingTask(0, false))}
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
    buildJobsScrapingTaskTimeout = p.getOrElse("buildJobsScrapingTaskTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'buildJobsScrapingTaskTimeout' not found.")
      buildJobsScrapingTaskTimeout})
    scrapTryMaxNumber = p.getOrElse("scrapTryMaxNumber", {
      logger.worn("[Worker.setParameters] Parameter 'scrapTryMaxNumber' not found.")
      scrapTryMaxNumber})
    scrapTryTimeout = p.getOrElse("scrapTryTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'scrapTryTimeout' not found.")
      scrapTryTimeout})}
  def getMetrics:(Int,Int,Double,Int) = {
    (queueSize,numberOfProcJob,lastParsingQuality,numberOfFailureParsed)}}

