package sifters.odesk.db

import java.util.Date

import util.db.DBProvider
import util.structures.FoundJobsRow
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.StaticQuery

/**
 * Provide DB access for oDesk sifters
 * Created by CAB on 03.11.2014.
 */

class ODeskSiftersDBProvider extends DBProvider{
  //Data methods
  def getUrlOfWrongParsedJobs:List[String] = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getUrlOfWrongParsedJobs] No created DB.")}
    db.get.withSession(implicit session => {
      StaticQuery.queryNA[String]("select distinct(" + urlColumn + ") from " + odesk_excavators_error_pages).list})}
  def findJobsWithNoAdditionalData:List[String] = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getUrlOfWrongParsedJobs] No created DB.")}
    db.get.withSession(implicit session => {
      StaticQuery.queryNA[String]("select distinct(" + urlColumn + ") from "
        + odesk_jobs + " where " + idColumn + " not in (select " + jobIdColumn
        + " from " + odesk_jobs_changes + ")").list})}
  def removeJobsByUrl(urls:List[String]):(Int,Int,Int,Int,Int,Int) = { //Return N del from: (jobTable,jobsChangesTable,clientsChangesTable,jobsApplicantsTable,jobsHiredTable,clientsWorksHistoryTable)
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getUrlOfWrongParsedJobs] No created DB.")}
    db.get.withSession(implicit session => {
      //Get ids of wrong job
      val ids = jobTable.filter(_.o_url inSetBind urls).map(_.id).list.flatMap(e => e)
      //Remove from all tables
      val nj = jobTable.filter(_.o_url inSetBind urls).delete
      val njc = jobsChangesTable.filter(_.job_id inSetBind ids).delete
      val ncc = clientsChangesTable.filter(_.job_id inSetBind ids).delete
      val na = jobsApplicantsTable.filter(_.job_id inSetBind ids).delete
      val nh = jobsHiredTable.filter(_.job_id inSetBind ids).delete
      val nw = clientsWorksHistoryTable.filter(_.job_id inSetBind ids).delete
      //Return amount
      (nj,njc,ncc,na,nh,nw)})}
  def getJobsUrlAndTitle:List[(String, Option[String])] = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getUrlOfWrongParsedJobs] No created DB.")}
    db.get.withSession(implicit session => {jobTable.map(r => (r.o_url,r.job_title)).list})}


}
