package excavators.odesk.db
import java.sql.Timestamp
import slick.driver.H2Driver.simple._
import slick.driver.H2Driver.backend.DatabaseDef
import scala.slick.jdbc.StaticQuery
import excavators.odesk.structures._
import java.util.Date
import java.io.{ByteArrayOutputStream, FileInputStream}
import javax.imageio.ImageIO

/**
* Provide DB access for another components
* Created by WORK on 22.09.14.
*/

class DBProvider {
  //Schema
  private type LogRowType = (Option[Long],Timestamp,String,String)
  private class ExcavatorsLog(tag: Tag) extends Table[LogRowType](tag, "odesk_excavators_log"){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def name = column[String]("name", O.NotNull)
    def msg = column[String]("msg")
    def * = (id,create_date, name, msg)}
  private val excavatorsLogTable = TableQuery[ExcavatorsLog]
  private type FoundJobsRowType = (Option[Long],String,String,Timestamp,Int,String,Option[Int])
  private class FoundJobs(tag: Tag) extends Table[FoundJobsRowType](tag, "odesk_found_jobs"){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def o_url = column[String]("o_url", O.NotNull)
    def found_by = column[String]("found_by", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def priority = column[Int]("priority", O.NotNull)
    def job_skills = column[String]("job_skills", O.NotNull)
    def n_freelancers = column[Option[Int]]("n_freelancers")
    def * = (id,o_url,found_by,create_date,priority,job_skills,n_freelancers)}
  private val foundJobsTableType = TableQuery[FoundJobs]
  private type JobRowType = (Option[Long],String,String,Timestamp,Timestamp,Option[Timestamp],
    Option[Timestamp],Option[Timestamp],Option[Timestamp],
    Option[Timestamp],Option[Int],Option[String],Option[String],String,Option[Double],String,
    Option[String],String,String,String,Option[String])
  private class Jobs(tag: Tag) extends Table[JobRowType](tag, "odesk_jobs"){
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
  private val jobTable = TableQuery[Jobs]
  private type JobsChangesRowType = (Option[Long],Long,Timestamp,Option[Timestamp],Option[Int],Option[Double],Option[Double],Option[Double],Option[Double],
    Option[Int],Option[Double],Option[Int])
  private class JobsChanges(tag: Tag) extends Table[JobsChangesRowType](tag, "odesk_jobs_changes"){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def last_viewed = column[Option[Timestamp]]("last_viewed")
    def n_applicants = column[Option[Int]]("n_applicants")
    def applicants_avg = column[Option[Double]]("applicants_avg")
    def rate_min = column[Option[Double]]("rate_min")
    def rate_avg = column[Option[Double]]("rate_avg")
    def rate_max = column[Option[Double]]("rate_max")
    def n_interviewing = column[Option[Int]]("n_interviewing")
    def interviewing_avg = column[Option[Double]]("interviewing_avg")
    def n_hires = column[Option[Int]]("n_hires")
    def * = (id,job_id,create_date,last_viewed,n_applicants,applicants_avg,rate_min,rate_avg,rate_max,
      n_interviewing,interviewing_avg,n_hires)}
  private val jobsChangesTable = TableQuery[JobsChanges]
  private type ClientsChangesRowType = (Option[Long],Long,Timestamp,Option[String],Option[Array[Byte]],Option[String],Option[String],
    String,Option[Double],Option[Int],Option[String],Option[String],Option[Int],Option[Int],Option[Int],
    Option[Double],Option[Int],Option[Int],Option[Double],Option[Int],Option[Timestamp])
  private class ClientsChanges(tag: Tag) extends Table[ClientsChangesRowType](tag, "odesk_clients_changes"){
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
  private val clientsChangesTable = TableQuery[ClientsChanges]
  private type JobsApplicantsRowType = (Option[Long],Long,Timestamp,Option[Timestamp],Option[String],String,Option[String],Option[Long])
  private class JobsApplicants(tag: Tag) extends Table[JobsApplicantsRowType](tag, "odesk_jobs_applicants"){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def up_date = column[Option[Timestamp]]("up_date")
    def name = column[Option[String]]("name")
    def initiated_by = column[String]("initiated_by")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (id,job_id,create_date,up_date,name,initiated_by,freelancer_url,freelancer_id)}
  private val jobsApplicantsTable = TableQuery[JobsApplicants]
  private type JobsHiredRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],Option[Long])
  private class JobsHired(tag: Tag) extends Table[JobsHiredRowType](tag, "odesk_jobs_hired"){
    def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def name = column[Option[String]]("name")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (id,job_id,create_date,name,freelancer_url,freelancer_id)}
  private val jobsHiredTable = TableQuery[JobsHired]
  private type ClientsWorksHistoryRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],String,Option[Timestamp],Option[Timestamp],
    String,Option[Double],Option[Int],Option[Double],Option[String],Option[Double],
    Option[String],Option[String],Option[Long],Option[Double])
  private class ClientsWorksHistor(tag: Tag) extends Table[ClientsWorksHistoryRowType](tag, "odesk_clients_works_history"){
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
  private val clientsWorksHistoryTable = TableQuery[ClientsWorksHistor]
  private type FoundFreelancersRowType = (Option[Long],String,Timestamp,Int)
  private class FoundFreelancers(tag: Tag) extends Table[FoundFreelancersRowType](tag, "odesk_found_freelancers"){
      def id = column[Option[Long]]("id",O.PrimaryKey, O.AutoInc)
      def o_url = column[String]("o_url", O.NotNull)
      def create_date = column[Timestamp]("create_date", O.NotNull)
      def priority = column[Int]("priority", O.NotNull)
      def * = (id,o_url,create_date,priority)}
  private val foundFreelancersTable = TableQuery[FoundFreelancers]
  //Variables
  private var db:Option[DatabaseDef] = None
  //Service methods
  def init(url:String, user:String, password:String, dbName:String) = {
    val b = Database.forURL(url + "/" + dbName, driver = "scala.slick.driver.MySQLDriver", user = user, password = password)
    b.withSession(implicit session => {
      StaticQuery.updateNA("SET GLOBAL SQL_MODE=ANSI_QUOTES;").execute})
    db = Some(b)}
  def halt() ={
    db = None}
  //Data methods
  def addLogMessage(date:Date, name:String, msg:String):Long = {
    if(db.isEmpty){throw new Exception("[DBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {excavatorsLogTable += (None, new Timestamp(date.getTime), name, msg)})}
  def addFoundJobsRow(d:FoundJobsRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {
      foundJobsTableType += (
        None,                          // id
        d.oUrl,                        // o_url
        d.foundBy.toString,            // found_by
        new Timestamp(d.date.getTime), //c reate_date
        d.priority,                    // priority
        d.skills.mkString(","),        // job_skills
        d.nFreelancers)})}             //n_freelancers
  def addJobsRow(d:JobsRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.addJobsRow] No created DB.")}
    db.get.withSession(implicit session => {
      jobTable += (
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
        d.jabData.jobDescription)})}                            // job_description Option[String]
  def addJobsChangesRow(d:JobsChangesRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.addJobsChangesRow] No created DB.")}
    db.get.withSession(implicit session => {
      jobsChangesTable += (
        None, // id
        d.jobId, // job_id
        new Timestamp(d.changeData.createDate.getTime),             // create_date
        d.changeData.lastViewed.map(t => new Timestamp(t.getTime)), // last_viewed
        d.changeData.nApplicants,                                   // n_applicants
        d.changeData.applicantsAvg,                                 // applicants_avg
        d.changeData.rateMin,                                       // rate_min
        d.changeData.rateAvg,                                       // rate_avg
        d.changeData.rateMax,                                       // rate_max
        d.changeData.nInterviewing,                                 // n_interviewing
        d.changeData.interviewingAvg,                               // interviewing_avg
        d.changeData.nHires)})}                                     // n_hires
  def addClientsChangesRow(d:ClientsChangesRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.addClientsChangesRow] No created DB.")}
    db.get.withSession(implicit session => {
      val ib = d.logo.map(i => {
        val ib = new ByteArrayOutputStream()
        ImageIO.write(i, "jpg", ib )
        ib.toByteArray})
      clientsChangesTable += (
        None,                                           // id
        d.jobId,                                        // job_id
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
        d.changeData.registrationDate.map(t => new Timestamp(t.getTime)))})} // client_registration_date
  def addJobsApplicantsRow(d:JobsApplicantsRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.addJobsApplicantsRow] No created DB.")}
    db.get.withSession(implicit session => {
      jobsApplicantsTable += (
        None,                                                      // id
        d.jobId,                                                   // job_id
        new Timestamp(d.applicantData.createDate.getTime),         // create_date
        d.applicantData.upDate.map(t => new Timestamp(t.getTime)), // up_date
        d.applicantData.name,                                      // name
        d.applicantData.initiatedBy.toString,                      // initiated_by
        d.applicantData.url,                                       // freelancer_url
        d.freelancerId)})}                                         // freelancer_id
  def addJobsHiredRow(d:JobsHiredRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.addJobsHiredRow] No created DB.")}
    db.get.withSession(implicit session => {
      jobsHiredTable += (
        None,                                          // id
        d.jobId,                                       // job_id
        new Timestamp(d.hiredData.createDate.getTime), // create_date
        d.hiredData.name,                              // name
        d.hiredData.freelancerUrl,                     // freelancer_url
        d.freelancerId)})}                             // freelancer_id
  def addClientsWorksHistoryRow(d:ClientsWorksHistoryRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.addClientsWorksHistoryRow] No created DB.")}
    db.get.withSession(implicit session => {
      clientsWorksHistoryTable += (
        None,                                                    // id
        d.jobId,                                                 // job_id
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
        d.workData.freelancerName,                               // freelancer_url
        d.freelancerId,                                          // freelancer_id
        d.workData.clientFeedback)})}                            // client_feedback
 def addFoundFreelancerRow(d:FoundFreelancerRow) = {
    if(db.isEmpty){throw new Exception("[DBProvider.addFoundFreelancerRow] No created DB.")}
   db.get.withSession(implicit session => {
     foundFreelancersTable += (
       None,                          // id
       d.oUrl,                        // o_url
       new Timestamp(d.date.getTime), // create_date
       d.priority)})}}                // priority













































