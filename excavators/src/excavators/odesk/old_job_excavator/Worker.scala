package excavators.odesk.old_job_excavator

import java.awt.image.BufferedImage
import java.util.Date
import javax.imageio.ImageIO
import excavators.odesk.db.{Saver, DBProvider}
import excavators.odesk.structures._
import excavators.odesk.sundries.AbstractWorker
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

class Worker(b:Browser, logger:Logger, saver:Saver, db:DBProvider) extends AbstractWorker(b,logger,db){
  //Construction
  super.setPaused(! runAfterStart)
  addTask(new BuildJobsScrapingTask(System.currentTimeMillis()))
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

