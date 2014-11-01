package util.db

import java.io.ByteArrayOutputStream
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import javax.imageio.ImageIO
import excavators.util.logging.LoggerDBProvider
import util.structures._
import scala.slick.driver.H2Driver.backend.DatabaseDef
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.StaticQuery

/**
* Provide DB access for another components
* Created by CAB on 22.09.14.
*/

trait DBProvider extends LoggerDBProvider {
  //Tables names
  protected val odesk_job_excavators_param = "odesk_job_excavators_param"
  protected val need_update_param_name = "param_need_update"
  protected val odesk_excavators_log = "odesk_excavators_log"
  protected val odesk_excavators_error_pages = "odesk_excavators_error_pages"
  protected val odesk_found_jobs = "odesk_found_jobs"
  protected val odesk_jobs = "odesk_jobs"
  protected val odesk_jobs_changes = "odesk_jobs_changes"
  protected val odesk_jobs_hired = "odesk_jobs_hired"
  protected val odesk_jobs_applicants = "odesk_jobs_applicants"
  protected val odesk_clients_changes = "odesk_clients_changes"
  protected val odesk_clients_works_history = "odesk_clients_works_history"
  protected val odesk_found_freelancers = "odesk_found_freelancers"
  //Columns names
  protected val daeDateColumn = "dae_date"
  protected val priorityColumn = "priority"
  protected val foundByColumn = "found_by"
  protected val nameColumn = "name"
  protected val typeColumn = "type"
  protected val logTypesNames = List("info","debug","worn","error")
  protected val jobNullableColumns = List(
    "post_date","deadline","dae_date","delete_date","next_check_date","n_freelancers",
    "job_title","job_type","job_price","job_length","job_description")
  protected val jobUnknowableColumns = List("job_payment_type","job_employment","job_required_level","found_by")
  protected val jobLists = List("job_skills","job_qualifications")
  protected val jobChangeNullableColumns = List("last_viewed","n_applicants","applicants_avg","rate_min",
    "rate_avg","rate_max","n_interviewing","interviewing_avg","n_hires")
  protected val jobChangeUnknowableColumns = List("available")
  protected val clientsChangesNullableColumns = List("name","logo","url","description","rating","n_reviews",
    "location","time","n_jobs","hire_rate","n_open_jobs","total_spend","n_hires","n_active",
    "avg_rate","hours","registration_date")
  protected val clientsChangesUnknowableColumns = List("payment_method")
  protected val applicantsNullableColumns = List("up_date","name","freelancer_url","freelancer_id")
  protected val applicantsUnknowableColumns = List("initiated_by")
  protected val hiredNullableColumns = List("name","freelancer_url","freelancer_id")
  protected val worksHistoryNullableColumns = List("o_url","title","start_date","end_date","billed",
    "hours","rate","freelancer_feedback_text","freelancer_feedback","freelancer_name",
    "freelancer_url","freelancer_id","client_feedback")
  protected val worksHistoryUnknowableColumns = List("in_progress","payment_type")
  //Fealds names
  val excavatorsStatesParamName = "excavatorsStates"
  //Helpers
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  //Schema
  protected type ParamRowType = (Option[Long],String,String,Boolean,Timestamp,Option[String])
  protected class ExcavatorsParam(tag: Tag) extends Table[ParamRowType](tag, odesk_job_excavators_param){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def p_key = column[String]("p_key", O.NotNull)
    def p_value = column[String]("p_value", O.NotNull)
    def is_active = column[Boolean]("is_active", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def comment = column[Option[String]]("comment")
    def * = (id,p_key,p_value,is_active,create_date,comment)}
  protected val excavatorsParamTable = TableQuery[ExcavatorsParam]
  protected type LogRowType = (Option[Long],Timestamp,String,String,String)
  protected class ExcavatorsLog(tag: Tag) extends Table[LogRowType](tag, odesk_excavators_log){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def mType = column[String]("type", O.NotNull)
    def name = column[String]("name", O.NotNull)
    def msg = column[String]("msg")
    def * = (id,create_date,mType, name, msg)}
  protected val excavatorsLogTable = TableQuery[ExcavatorsLog]
  protected type ParsingErrorsRowType = (Option[Long],Timestamp,String,String,String)
  protected class ParsingErrors(tag: Tag) extends Table[ParsingErrorsRowType](tag, odesk_excavators_error_pages){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def o_url = column[String]("o_url", O.NotNull)
    def msg = column[String]("msg", O.NotNull)
    def html = column[String]("html", O.NotNull)
    def * = (id,create_date,o_url,msg,html)}
  protected val parsingErrorsTable = TableQuery[ParsingErrors]
  protected type FoundJobsRowType = (Option[Long],String,String,Timestamp,Int,String,Option[Int])
  protected class FoundJobs(tag: Tag) extends Table[FoundJobsRowType](tag, odesk_found_jobs){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def o_url = column[String]("o_url", O.NotNull)
    def found_by = column[String]("found_by", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def priority = column[Int]("priority", O.NotNull)
    def job_skills = column[String]("job_skills", O.NotNull)
    def n_freelancers = column[Option[Int]]("n_freelancers")
    def * = (id,o_url,found_by,create_date,priority,job_skills,n_freelancers)}
  protected val foundJobsTable = TableQuery[FoundJobs]
  protected type JobRowType = (Option[Long],String,String,Timestamp,Timestamp,Option[Timestamp],
    Option[Timestamp],Option[Timestamp],Option[Timestamp],
    Option[Timestamp],Option[Int],Option[String],Option[String],String,Option[Double],String,
    Option[String],String,String,String,Option[String])
  protected class Jobs(tag: Tag) extends Table[JobRowType](tag, odesk_jobs){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def o_url = column[String]("o_url", O.NotNull)
    def found_by = column[String]("found_by", O.NotNull)
    def found_date = column[Timestamp]("found_date", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def post_date = column[Option[Timestamp]]("post_date")
    def deadline = column[Option[Timestamp]]("deadline")
    def dae_date = column[Option[Timestamp]]("dae_date")
    def delete_date = column[Option[Timestamp]]("delete_date")
    def next_check_date = column[Option[Timestamp]]("next_check_date")
    def n_freelancers = column[Option[Int]]("n_freelancers")
    def job_title = column[Option[String]]("job_title")
    def job_type = column[Option[String]]("job_type")
    def job_payment_type = column[String]("job_payment_type", O.NotNull)
    def job_price = column[Option[Double]]("job_price")
    def job_employment = column[String]("job_employment", O.NotNull)
    def job_length = column[Option[String]]("job_length")
    def job_required_level = column[String]("job_required_level", O.NotNull)
    def job_skills = column[String]("job_skills", O.NotNull)
    def job_qualifications = column[String]("job_qualifications", O.NotNull)
    def job_description = column[Option[String]]("job_description")
    def * = (id,o_url,found_by,found_date,create_date,post_date,deadline,dae_date,delete_date,
      next_check_date,n_freelancers,job_title,job_type,job_payment_type,job_price,job_employment,job_length,
      job_required_level,job_skills,job_qualifications,job_description)}
  protected val jobTable = TableQuery[Jobs]
  protected type JobsChangesRowType = (Option[Long],Long,Timestamp,String,Option[Timestamp],Option[Int],Option[Double],Option[Double],Option[Double],Option[Double],
    Option[Int],Option[Double],Option[Int])
  protected class JobsChanges(tag: Tag) extends Table[JobsChangesRowType](tag, odesk_jobs_changes){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def available = column[String]("available", O.NotNull)
    def last_viewed = column[Option[Timestamp]]("last_viewed")
    def n_applicants = column[Option[Int]]("n_applicants")
    def applicants_avg = column[Option[Double]]("applicants_avg")
    def rate_min = column[Option[Double]]("rate_min")
    def rate_avg = column[Option[Double]]("rate_avg")
    def rate_max = column[Option[Double]]("rate_max")
    def n_interviewing = column[Option[Int]]("n_interviewing")
    def interviewing_avg = column[Option[Double]]("interviewing_avg")
    def n_hires = column[Option[Int]]("n_hires")
    def * = (id,job_id,create_date,available,last_viewed,n_applicants,applicants_avg,rate_min,rate_avg,rate_max,
      n_interviewing,interviewing_avg,n_hires)}
  protected val jobsChangesTable = TableQuery[JobsChanges]
  protected type ClientsChangesRowType = (Option[Long],Long,Timestamp,Option[String],Option[Array[Byte]],Option[String],Option[String],
    String,Option[Double],Option[Int],Option[String],Option[String],Option[Int],Option[Int],Option[Int],
    Option[Double],Option[Int],Option[Int],Option[Double],Option[Int],Option[Timestamp])
  protected class ClientsChanges(tag: Tag) extends Table[ClientsChangesRowType](tag, odesk_clients_changes){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def name = column[Option[String]]("name")
    def logo = column[Option[Array[Byte]]]("logo")
    def url = column[Option[String]]("url")
    def description = column[Option[String]]("description")
    def payment_method = column[String]("payment_method")
    def rating = column[Option[Double]]("rating")
    def n_reviews = column[Option[Int]]("n_reviews")
    def location = column[Option[String]]("location")
    def time = column[Option[String]]("time")
    def n_jobs = column[Option[Int]]("n_jobs")
    def hire_rate = column[Option[Int]]("hire_rate")
    def n_open_jobs = column[Option[Int]]("n_open_jobs")
    def total_spend = column[Option[Double]]("total_spend")
    def n_hires = column[Option[Int]]("n_hires")
    def n_active = column[Option[Int]]("n_active")
    def avg_rate = column[Option[Double]]("avg_rate")
    def hours = column[Option[Int]]("hours")
    def registration_date = column[Option[Timestamp]]("registration_date")
    def * = (id,job_id,create_date,name,logo,url,description,payment_method,rating,n_reviews,location,time,n_jobs,
      hire_rate,n_open_jobs,total_spend,n_hires,n_active,avg_rate,hours,registration_date)}
  protected val clientsChangesTable = TableQuery[ClientsChanges]
  protected type JobsApplicantsRowType = (Option[Long],Long,Timestamp,Option[Timestamp],Option[String],String,Option[String],Option[Long])
  protected class JobsApplicants(tag: Tag) extends Table[JobsApplicantsRowType](tag, odesk_jobs_applicants){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def up_date = column[Option[Timestamp]]("up_date")
    def name = column[Option[String]]("name")
    def initiated_by = column[String]("initiated_by")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (id,job_id,create_date,up_date,name,initiated_by,freelancer_url,freelancer_id)}
  protected val jobsApplicantsTable = TableQuery[JobsApplicants]
  protected type JobsHiredRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],Option[Long])
  protected class JobsHired(tag: Tag) extends Table[JobsHiredRowType](tag, odesk_jobs_hired){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def name = column[Option[String]]("name")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (id,job_id,create_date,name,freelancer_url,freelancer_id)}
  protected val jobsHiredTable = TableQuery[JobsHired]
  protected type ClientsWorksHistoryRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],String,Option[Timestamp],Option[Timestamp],
    String,Option[Double],Option[Int],Option[Double],Option[String],Option[Double],
    Option[String],Option[String],Option[Long],Option[Double])
  protected class ClientsWorksHistor(tag: Tag) extends Table[ClientsWorksHistoryRowType](tag, odesk_clients_works_history){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def o_url = column[Option[String]]("o_url")
    def title = column[Option[String]]("title")
    def in_progress = column[String]("in_progress")
    def start_date = column[Option[Timestamp]]("start_date")
    def end_date = column[Option[Timestamp]]("end_date")
    def payment_type = column[String]("payment_type")
    def billed = column[Option[Double]]("billed")
    def hours = column[Option[Int]]("hours")
    def rate = column[Option[Double]]("rate")
    def freelancer_feedback_text = column[Option[String]]("freelancer_feedback_text")
    def freelancer_feedback = column[Option[Double]]("freelancer_feedback")
    def freelancer_name = column[Option[String]]("freelancer_name")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def client_feedback = column[Option[Double]]("client_feedback")
    def * = (id,job_id,create_date,o_url,title,in_progress,start_date,end_date,payment_type,billed,hours,rate,
      freelancer_feedback_text,freelancer_feedback,freelancer_name,freelancer_url,freelancer_id,client_feedback)}
  protected val clientsWorksHistoryTable = TableQuery[ClientsWorksHistor]
  protected type FoundFreelancersRowType = (Option[Long],String,Timestamp,Int)
  protected class FoundFreelancers(tag: Tag) extends Table[FoundFreelancersRowType](tag, odesk_found_freelancers){
      def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
      def o_url = column[String]("o_url", O.NotNull)
      def create_date = column[Timestamp]("create_date", O.NotNull)
      def priority = column[Int]("priority", O.NotNull)
      def * = (id,o_url,create_date,priority)}
  protected val foundFreelancersTable = TableQuery[FoundFreelancers]
  //Tables map
  protected val tablesByName = Map(
    odesk_job_excavators_param -> excavatorsParamTable,
    odesk_excavators_log -> excavatorsLogTable,
    odesk_excavators_error_pages -> parsingErrorsTable,
    odesk_found_jobs -> foundJobsTable,
    odesk_jobs -> jobTable,
    odesk_jobs_changes -> jobsChangesTable,
    odesk_jobs_hired -> jobsHiredTable,
    odesk_jobs_applicants -> jobsApplicantsTable,
    odesk_clients_changes -> clientsChangesTable,
    odesk_clients_works_history -> clientsWorksHistoryTable,
    odesk_found_freelancers -> foundFreelancersTable)
  //Variables
  protected var db:Option[DatabaseDef] = None
  protected var databaseName:Option[String] = None
  //Functions
  protected def buildFoundJobsRows(id:Option[Long], url:String, fb:String, d:Timestamp, p:Int, sks:String, nf:Option[Int]):FoundJobsRow = FoundJobsRow(
    id = id.get,
    oUrl = url,
    foundBy = FoundBy.formString(fb),
    date = new Date(d.getTime),
    priority = p,
    skills = sks.split(",").toList,
    nFreelancers = nf)
  protected def buildFoundJobsRow(d:FoundJobsRow,p:Int):FoundJobsRowType = {(
    None,                          // id
    d.oUrl,                        // o_url
    d.foundBy.toString,            // found_by
    new Timestamp(d.date.getTime), // reate_date
    p,                             // priority
    d.skills.mkString(","),        // job_skills
    d.nFreelancers)}               //n_freelancers
  protected def buildJobsRow(d:JobsRow):JobRowType = { (
    None,                                                   // id Option[Long]
    d.foundData.oUrl,                                       // o_url String
    d.foundData.foundBy.toString,                           // found_by String
    new Timestamp(d.foundData.date.getTime),                // found_date Timestamp
    new Timestamp(d.jabData.createDate.getTime),            // create_date Timestamp
    d.jabData.postDate.map(t => new Timestamp(t.getTime)),  // post_date Option[Timestamp]
    d.jabData.deadline.map(t => new Timestamp(t.getTime)),  // deadline Option[Timestamp]
    d.daeDate.map(t => new Timestamp(t.getTime)),           // dae_date Option[Timestamp]
    d.deleteDate.map(t => new Timestamp(t.getTime)),        // delete_date Option[Timestamp]
    d.nextCheckDate.map(t => new Timestamp(t.getTime)),     // next_check_date Option[Timestamp]
    d.foundData.nFreelancers,                               // n_freelancers Option[Int]
    d.jabData.jobTitle,                                     // job_title Option[String]
    d.jabData.jobType,                                      // job_type Option[String]
    d.jabData.jobPaymentType.toString,                      // job_payment_type Option[String]
    d.jabData.jobPrice,                                     // job_price Option[Double]
    d.jabData.jobEmployment.toString,                       // job_employment Option[String]
    d.jabData.jobLength,                                    // job_length Option[String]
    d.jabData.jobRequiredLevel.toString,                    // job_required_level Option[String]
    d.foundData.skills.mkString(","),                       // job_skills Option[String]
    d.jabData.jobQualifications.map{case (k,v) => {k + "$" + v}}.mkString("|"), // job_qualifications Option[String]
    d.jabData.jobDescription)}                              // job_description Option[String]
  protected def buildJobsChangesRow(d:JobsChangesRow, jid:Long):JobsChangesRowType = {(
    None, // id
    jid, // job_id
    new Timestamp(d.changeData.createDate.getTime),             // create_date
    d.changeData.jobAvailable.toString,                         // available
    d.changeData.lastViewed.map(t => new Timestamp(t.getTime)), // last_viewed
    d.changeData.nApplicants,                                   // n_applicants
    d.changeData.applicantsAvg,                                 // applicants_avg
    d.changeData.rateMin,                                       // rate_min
    d.changeData.rateAvg,                                       // rate_avg
    d.changeData.rateMax,                                       // rate_max
    d.changeData.nInterviewing,                                 // n_interviewing
    d.changeData.interviewingAvg,                               // interviewing_avg
    d.changeData.nHires)}                                       // n_hires
  protected def buildClientsChangesRow(d:ClientsChangesRow, jid:Long):ClientsChangesRowType = {
    //Prepare
    val ib = d.logo.map(i => {
      val ib = new ByteArrayOutputStream()
      ImageIO.write(i, "jpg", ib )
      ib.toByteArray})
    //Build
    ( None,                                           // id
      jid,                                        // job_id
      new Timestamp(d.changeData.createDate.getTime), // create_date
      d.changeData.name,                              // client_name
      ib,                                             // client_logo
      d.changeData.url,                               // client_url
      d.changeData.description,                       // client_description
      d.changeData.paymentMethod.toString,            // client_payment_method
      d.changeData.rating,                            // client_rating
      d.changeData.nReviews,                          // client_n_reviews
      d.changeData.location,                          // client_location
      d.changeData.time,                              // client_time
      d.changeData.nJobs,                             // client_n_jobs
      d.changeData.hireRate,                          // client_hire_rate
      d.changeData.nOpenJobs,                         // client_n_open_jobs
      d.changeData.totalSpend,                        // client_total_spend
      d.changeData.nHires,                            // client_n_hires
      d.changeData.nActive,                           // client_n_active
      d.changeData.avgRate,                           // client_avg_rate
      d.changeData.hours,                             // client_hours
      d.changeData.registrationDate.map(t => new Timestamp(t.getTime)))} // client_registration_date
  protected def buildJobsApplicantsRows(d:JobsApplicantsRow, jid:Long):JobsApplicantsRowType = (
    None,                                                      // id
    jid,                                                       // job_id
    new Timestamp(d.applicantData.createDate.getTime),         // create_date
    d.applicantData.upDate.map(t => new Timestamp(t.getTime)), // up_date
    d.applicantData.name,                                      // name
    d.applicantData.initiatedBy.toString,                      // initiated_by
    d.applicantData.url,                                       // freelancer_url
    d.freelancerId)                                            // freelancer_id
  protected def buildJobsHiredRows(d:JobsHiredRow, jid:Long):JobsHiredRowType = (
    None,                                          // id
    jid,                                           // job_id
    new Timestamp(d.hiredData.createDate.getTime), // create_date
    d.hiredData.name,                              // name
    d.hiredData.freelancerUrl,                     // freelancer_url
    d.freelancerId)                                // freelancer_id
  protected def buildClientsWorksHistoryRow(d:ClientsWorksHistoryRow, jid:Long):ClientsWorksHistoryRowType = (
    None,                                                    // id
    jid,                                                     // job_id
    new Timestamp(d.workData.createDate.getTime),            // create_date
    d.workData.oUrl,                                         // o_url
    d.workData.title,                                        // title
    d.workData.inProgress.toString,                          // in_progress
    d.workData.startDate.map(t => new Timestamp(t.getTime)), // start_date
    d.workData.endDate.map(t => new Timestamp(t.getTime)),   // end_date
    d.workData.paymentType.toString,                         // payment_type
    d.workData.billed,                                       // billed
    d.workData.hours,                                        // hours
    d.workData.rate,                                         // rate
    d.workData.freelancerFeedbackText,                       // freelancer_feedback_text
    d.workData.freelancerFeedback,                           // freelancer_feedback
    d.workData.freelancerName,                               // freelancer_name
    d.workData.freelancerUrl,                                // freelancer_url
    d.freelancerId,                                          // freelancer_id
    d.workData.clientFeedback)                               // client_feedback
  protected def buildFoundFreelancerRow(d:FoundFreelancerRow):FoundFreelancersRowType = (
    None,                          // id
    d.oUrl,                        // o_url
    new Timestamp(d.date.getTime), // create_date
    d.priority)                    // priority
  protected def countRows(tableName:String):Int = {
    if(db.isEmpty){throw new Exception("[DBProvider.countRows] No created DB.")}
    if(! tablesByName.contains(tableName)){throw new Exception("[DBProvider.countRows] Unknown table name: " + tableName)}
    db.get.withSession(implicit session => {tablesByName(tableName).length.run})}
  protected def buildDateCondition(from:Option[Date], to:Option[Date]):Option[String] = {
    val mnd = from match{case Some(d) => "create_date >= '" + dateFormat.format(d) + "'"; case _ => ""}
    val mxd = to match{case Some(d) => "create_date <= '" + dateFormat.format(d) + "'"; case _ => ""}
    (mnd, mxd) match{
      case ("","") => None
      case (_,"") => Some(mnd)
      case ("",_) => Some(mxd)
      case _ => Some(mnd + " and " + mxd)}}
  //Service methods
  def init(url:String, user:String, password:String, dbName:String) = {
    val b = Database.forURL(url + "/" + dbName, driver = "scala.slick.driver.MySQLDriver", user = user, password = password)
    b.withSession(implicit session => {
      StaticQuery.updateNA("SET GLOBAL SQL_MODE=ANSI_QUOTES;").execute})
    db = Some(b)
    databaseName = Some(dbName)}
  def halt() ={
    db = None}
  //Data methods
  def addLogMessageRow(date:Date, mType:String, name:String, msg:String) = {
    if(db.isEmpty){throw new Exception("[DBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {excavatorsLogTable += (None, new Timestamp(date.getTime), mType, name, msg)})}}
























































































