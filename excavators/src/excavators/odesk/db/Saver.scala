package excavators.odesk.db

import excavators.odesk.structures._
import excavators.util.logging.Logger
import excavators.util.tasks.{Task, TaskExecutor}

/**
* Component responsible for parallel save data to DB.
* Created by CAB on 20.10.2014.
*/

class Saver(logger:Logger, db:DBProvider) extends TaskExecutor{
  //Parameters
  val maxTaskQueueSize = 10000
  val overloadTimeout = 1000
  //Variables
  private var saveTime = 0L
  //Functions
  private def checkOverload() = if(queueSize > maxTaskQueueSize){
    logger.worn("[Saver.checkOverload] Queue overload, queueSize = " + queueSize)
    while(isWork && (queueSize > maxTaskQueueSize)){Thread.sleep(overloadTimeout)}}
  //Tasks
  case class SaveJobAdditionalDataTask(d:(JobsChangesRow, ClientsChangesRow, List[JobsApplicantsRow], List[JobsHiredRow],
    List[ClientsWorksHistoryRow], Set[FoundFreelancerRow], Set[FoundJobsRow])) extends Task(1) {
    def execute() = d match{case ((jcr,ccr,ars,jhr,whr,ffr,fjr)) => {
      //Start
      val ct = System.currentTimeMillis()
      //Save
      try{
        db.addJobsChangesRow(jcr)
        logger.info("[Saver.SaveJobAdditionalDataTask] Added job changes, job id = " + jcr.jobId)
        db.addClientsChangesRow(ccr)
        logger.info("[Saver.SaveJobAdditionalDataTask] Added client changes, job id = " + jcr.jobId)
        ars.foreach(r => db.addJobsApplicantsRow(r))
        logger.info("[Saver.SaveJobAdditionalDataTask] Added " + ars.size + " applicants, job id = " + jcr.jobId)
        jhr.foreach(r => db.addJobsHiredRow(r))
        logger.info("[Saver.SaveJobAdditionalDataTask] Added " + jhr.size + " jobs hired, job id = " + jcr.jobId)
        val ncw = whr.map(r => if(db.addClientsWorksHistoryRow(r)) 1 else 0).sum
        logger.info("[Saver.SaveJobAdditionalDataTask] Added " + ncw + " (of "  + whr.size + ") of clients works history, job id = " + jcr.jobId)
        val nfi = ffr.toList.map(r => if(db.addFoundFreelancerRow(r)) 1 else 0).sum
        logger.info("[Saver.SaveJobAdditionalDataTask] Added " + nfi + " (of " + ffr.size + ") of found freelancer, job id = " + jcr.jobId)
        val nji = fjr.toList.map(r => if(db.addFoundJobsRow(r)) 1 else 0).sum
        logger.info("[Saver.SaveJobAdditionalDataTask] Added " + nji + " (of " + fjr.size + ") of found jobs, job id = " + jcr.jobId)}
      catch{case e:Exception =>
        logger.error("[Saver.SaveJobAdditionalDataTask] Exception on save job data: " + e)}
      //Save time
      saveTime = System.currentTimeMillis() - ct}}}
  case class DelFoundJobTas(j:FoundJobsRow) extends Task(2) {
    def execute() = {
      try{
        db.delFoundJobRow(j.id)}
      catch{case e:Exception => {
        logger.error("[Saver.DelFoundJobTas] Exception on dell found job: " + e + ", url=" + j.oUrl)}}}}
  //Methods
  def addSaveJobAdditionalDataAndDelFoundTask(d:(JobsChangesRow, ClientsChangesRow, List[JobsApplicantsRow], List[JobsHiredRow],
    List[ClientsWorksHistoryRow], Set[FoundFreelancerRow], Set[FoundJobsRow])) = {
    checkOverload()
    addTask(new SaveJobAdditionalDataTask(d))}
  def addDelFoundJobTask(j:FoundJobsRow) = {
    checkOverload()
    addTask(new DelFoundJobTas(j))}
  def getMetrics:(Int,Long) = (queueSize,saveTime)} //Return: (queue size, save time)





