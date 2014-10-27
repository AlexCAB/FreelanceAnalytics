package excavators.odesk.db

import java.awt.image.BufferedImage
import java.util.Date

import excavators.odesk.structures._
import excavators.util.logging.Logger
import excavators.util.parameters.ParametersMap
import excavators.util.tasks.{Task, TaskExecutor}

/**
* Component responsible for parallel save data to DB.
* Created by CAB on 20.10.2014.
*/

class Saver(logger:Logger, db:DBProvider) extends TaskExecutor{
  //Parameters
  val maxTaskQueueSize = 10000
  val overloadTimeout = 1000
  private var foundFreelancersPriority = 1
  private var jobsFoundByAnalisePriority = 1
  private var toTrackingJobPriority = 1
  private var nextJobCheckTimeout = 1000 * 60 * 60
  //Variables
  private var saveTime = 0L
  //Functions
  private def checkOverload() = if(queueSize > maxTaskQueueSize){
    logger.worn("[Saver.checkOverload] Queue overload, queueSize = " + queueSize)
    while(isWork && (queueSize > maxTaskQueueSize)){Thread.sleep(overloadTimeout)}}
  private def getFreelancerIdByUrl(oUrl:Option[String]):Option[Long] = {
    oUrl.flatMap(url => {
      try{
        db.getFreelancerIdByURL(url)}
      catch{case e:Exception => {
        logger.error("[Worker.getFreelancerIdByUrl] Exception: " + e)
        None}}})}
  private def prepareJobDataToSave(pj:ParsedJob, cd:Date, j:FoundJobsRow, logo:Option[BufferedImage]):AllJobData = {
    //Prepare JobsRow structure
    val jr = {
      //Date of next check
      val ncd = new Date(cd.getTime + nextJobCheckTimeout)
      //build
      JobsRow(
        id = 0,
        foundData = j,
        daeDate = if(pj.jobChanges.jobAvailable == JobAvailable.No){Some(cd)}else{None}, //If job not available set dae date
        deleteDate = None,
        nextCheckDate = if(pj.jobChanges.jobAvailable != JobAvailable.No){Some(ncd)}else{None}, //If job not available not add to tracking
        jabData = pj.job)}
    //Prepare JobsChangesRow structure
    val jcr = JobsChangesRow(
      id = 0,
      jobId = -1,
      changeData = pj.jobChanges)
    //Prepare ClientsChangesRow structure
    val ccr = ClientsChangesRow(
      id = 0,
      jobId = -1,
      changeData = pj.clientChanges,
      logo = logo)
    //Prepare JobsApplicantsRow structures
    val ars = pj.applicants.map(a => {
      JobsApplicantsRow(
        id = 0,
        jobId = -1,
        applicantData = a,
        freelancerId = getFreelancerIdByUrl(a.url))})
    //Prepare JobHired structures
    val jhr = pj.hires.map(h => {
      JobsHiredRow(
        id = 0,
        jobId = -1,
        hiredData = h,
        freelancerId = getFreelancerIdByUrl(h.freelancerUrl))})
    //Prepare ClientsWorksHistoryRow structures
    val whr = pj.clientWorks.map(w => {
      ClientsWorksHistoryRow(
        id = 0,
        jobId = -1,
        workData = w,
        freelancerId = getFreelancerIdByUrl(w.freelancerUrl))})
    //Prepare FoundFreelancerRow structures
    val ffr = {
      //Get freelancers urls
      val fus = pj.applicants.flatMap(_.url).toSet ++
        pj.hires.flatMap(_.freelancerUrl).toSet ++
        pj.clientWorks.flatMap(_.freelancerUrl).toSet
      //Build
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
      //Build
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
    AllJobData(
      foundJobsRow = j,
      jobsRow = jr,
      jobsChangesRow = jcr,
      clientsChangesRow = ccr,
      jobsApplicantsRows = ars.toList,
      jobsHiredRows = jhr,
      clientsWorksHistoryRows = whr,
      foundFreelancerRows = ffr.toList,
      foundJobsRows = fjr.toList)}
  //Tasks
  case class SaveJobDataTask(j:FoundJobsRow, pj:ParsedJob, pq:Double, cd:Date, logo:Option[BufferedImage]) extends Task(1) {
    def execute() = {
      //Start
      val ct = System.currentTimeMillis()
      //Preparing data to save
      val d = prepareJobDataToSave(pj, cd, j, logo)
      //Save data
      val r = try{
        db.addAllJobDataAndDelFromFound(d)} //Some(applicants,hired,clients works,found freelancer,found jobs)
      catch{case e:Exception =>{
        logger.error("[Saver.SaveJobDataTask] Exception on save job data: " + e)
        None}}
      //Logging
      r match{
        case Some((na,nh,ncw,nff,nfj)) => {
          logger.info("[Saver.SaveJobDataTask] Job added to DB, url: " + j.oUrl + ", with: "
            + na + "(of " + d.jobsApplicantsRows.size + ") applicants, "
            + nh + "(of " + d.jobsHiredRows.size + ") hired, "
            + ncw + "(of " + d.clientsWorksHistoryRows.size + ") clients works, "
            + nff + "(of " + d.foundFreelancerRows.size + ") found freelancer, "
            + nfj + "(of " + d.foundJobsRows.size + ") found jobs.")}
        case None => logger.worn("[Saver.SaveJobDataTask] Job not added to DB, url: " + j.oUrl)}       //Save time
      saveTime = System.currentTimeMillis() - ct}}
  case class DelFoundJobTas(j:FoundJobsRow) extends Task(2) {
    def execute() = {
      try{
        db.delFoundJobRow(j.id)}
      catch{case e:Exception => {
        logger.error("[Saver.DelFoundJobTas] Exception on dell found job: " + e + ", url=" + j.oUrl)}}}}
  //Methods
  def setParameters(p:ParametersMap) = {
    foundFreelancersPriority = p.getOrElse("foundFreelancersPriority", {
      logger.worn("[Worker.setParameters] Parameter 'foundFreelancersPriority' not found.")
      foundFreelancersPriority})
    jobsFoundByAnalisePriority = p.getOrElse("jobsFoundByAnalisePriority", {
      logger.worn("[Worker.setParameters] Parameter 'jobsFoundByAnalisePriority' not found.")
      jobsFoundByAnalisePriority})
    toTrackingJobPriority = p.getOrElse("toTrackingJobPriority", {
      logger.worn("[Worker.setParameters] Parameter 'toTrackingJobPriority' not found.")
      toTrackingJobPriority})
    nextJobCheckTimeout = p.getOrElse("nextJobCheckTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'nextJobCheckTimeout' not found.")
      nextJobCheckTimeout})}
  def addSaveJobDataAndDelFoundTask(j:FoundJobsRow, pj:ParsedJob, pq:Double, cd:Date, logo:Option[BufferedImage]) = {
    checkOverload()
    addTask(new SaveJobDataTask(j,pj,pq,cd,logo))}
  def addDelFoundJobTask(j:FoundJobsRow) = {
    checkOverload()
    addTask(new DelFoundJobTas(j))}
  def getMetrics:(Int,Long) = (queueSize, saveTime)} //Return: (queue size, save time)












