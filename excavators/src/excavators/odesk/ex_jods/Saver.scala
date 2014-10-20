package excavators.odesk.ex_jods

import excavators.odesk.db.DBProvider
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
  //Functions
  private def checkOverload() = if(queueSize > maxTaskQueueSize){
    logger.worn("[Saver.checkOverload] Queue overload, queueSize = " + queueSize)
    while(isWork && (queueSize > maxTaskQueueSize)){Thread.sleep(overloadTimeout)}}
  //Tasks
  case class SaveJobAdditionalDataAndDelFoundTask(d:(JobsChangesRow, ClientsChangesRow, List[JobsApplicantsRow], List[JobsHiredRow],
    List[ClientsWorksHistoryRow], Set[FoundFreelancerRow], Set[FoundJobsRow])) extends Task(1) {
    def execute() = d match{case ((jcr,ccr,ars,jhr,whr,ffr,fjr)) => {
      try{
        db.addJobsChangesRow(jcr)
        logger.info("[Saver.SaveJobAdditionalDataAndDelFoundTask] Added job changes, job id = " + jcr.jobId)
        db.addClientsChangesRow(ccr)
        logger.info("[Saver.SaveJobAdditionalDataAndDelFoundTask] Added client changes, job id = " + jcr.jobId)
        ars.foreach(r => db.addJobsApplicantsRow(r))
        logger.info("[Saver.SaveJobAdditionalDataAndDelFoundTask] Added " + ars.size + " applicants, job id = " + jcr.jobId)
        jhr.foreach(r => db.addJobsHiredRow(r))
        logger.info("[Saver.SaveJobAdditionalDataAndDelFoundTask] Added " + jhr.size + " jobs hired, job id = " + jcr.jobId)
        whr.foreach(r => db.addClientsWorksHistoryRow(r))
        logger.info("[Saver.SaveJobAdditionalDataAndDelFoundTask] Added " + whr.size + " clients works history, job id = " + jcr.jobId)
        ffr.foreach(r => db.addFoundFreelancerRow(r))
        logger.info("[Saver.SaveJobAdditionalDataAndDelFoundTask] Added " + ffr.size + " found freelancer, job id = " + jcr.jobId)
        fjr.foreach(r => db.addFoundJobsRow(r))
        logger.info("[Saver.SaveJobAdditionalDataAndDelFoundTask] Added " + fjr.size + " found jobs, job id = " + jcr.jobId)}
      catch{case e:Exception =>
        logger.error("[Saver.SaveJobAdditionalDataAndDelFoundTask] Exception on save job data: " + e)}}}}
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
    addTask(new SaveJobAdditionalDataAndDelFoundTask(d))}
  def addDelFoundJobTask(j:FoundJobsRow) = {
    checkOverload()
    addTask(new DelFoundJobTas(j))}}
