package excavators.odesk.db
import java.sql.{Blob, Date}
import slick.driver.H2Driver.simple._
import slick.driver.H2Driver.backend.DatabaseDef
import excavators.odesk.structures._

/**
* Provide DB access for another components
* Created by WORK on 22.09.14.
*/

class DBProvider {
  //Schema
  private type LogRowType = (Date,String,String)
  private class ExcavatorsLog(tag: Tag) extends Table[LogRowType](tag, "odesk_excavators_log"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def create_date = column[Date]("create_date", O.NotNull)
    def name = column[String]("name", O.NotNull)
    def msg = column[String]("msg")
    def * = (create_date, name, msg)}
  private val excavatorsLogTable = TableQuery[ExcavatorsLog]
  private type FoundJobsRowType = (String,String,Date,Int,String,Option[Int])
  private class FoundJobs(tag: Tag) extends Table[FoundJobsRowType](tag, "odesk_found_jobs"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def o_url = column[String]("o_url", O.NotNull)
    def found_by = column[String]("found_by", O.NotNull)
    def create_date = column[Date]("create_date", O.NotNull)
    def priority = column[Int]("priority", O.NotNull)
    def job_skills = column[String]("job_skills", O.NotNull)
    def n_freelancers = column[Option[Int]]("n_freelancers")
    def * = (o_url,found_by,create_date,priority,job_skills,n_freelancers)}
  private val foundJobsTableType = TableQuery[FoundJobs]
  private type JobRowType = (String,String,Date,Date,Option[Date],Option[Date],Option[Date],Option[Date],
    Option[Date],Option[Int],Option[String],Option[String],Option[String],Option[Double],Option[String],
    Option[String],Option[String],Option[String],Option[String],Option[String])
  private class Jobs(tag: Tag) extends Table[JobRowType](tag, "odesk_jobs"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def o_url = column[String]("o_url", O.NotNull)
    def found_by = column[String]("found_by", O.NotNull)
    def found_date = column[Date]("found_date", O.NotNull)
    def create_date = column[Date]("create_date", O.NotNull)
    def post_date = column[Option[Date]]("post_date")
    def deadline = column[Option[Date]]("deadline")
    def dae_date = column[Option[Date]]("dae_date")
    def delete_date = column[Option[Date]]("delete_date")
    def next_check_date = column[Option[Date]]("next_check_date")
    def n_freelancers = column[Option[Int]]("n_freelancers")
    def job_title = column[Option[String]]("job_title")
    def job_type = column[Option[String]]("job_type")
    def job_payment_type = column[Option[String]]("job_payment_type")
    def job_price = column[Option[Double]]("job_price")
    def job_employment = column[Option[String]]("job_employment")
    def job_length = column[Option[String]]("job_length")
    def job_required_level = column[Option[String]]("job_required_level")
    def job_skills = column[Option[String]]("job_skills")
    def job_qualifications = column[Option[String]]("job_qualifications")
    def job_description = column[Option[String]]("job_description")
    def * = (o_url,found_by,found_date,create_date,post_date,deadline,dae_date,delete_date,
      next_check_date,n_freelancers,job_title,job_type,job_payment_type,job_price,job_employment,job_length,
      job_required_level,job_skills,job_qualifications,job_description)}
  private val jobTable = TableQuery[Jobs]
  private type JobsChangesRowType = (Long,Date,Option[Date],Option[Int],Option[Double],Option[Double],Option[Double],Option[Double],
    Option[Int],Option[Double],Option[Int])
  private class JobsChanges(tag: Tag) extends Table[JobsChangesRowType](tag, "odesk_jobs_changes"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Date]("create_date", O.NotNull)
    def last_viewed = column[Option[Date]]("last_viewed")
    def n_applicants = column[Option[Int]]("n_applicants")
    def applicants_avg = column[Option[Double]]("applicants_avg")
    def rate_min = column[Option[Double]]("rate_min")
    def rate_avg = column[Option[Double]]("rate_avg")
    def rate_max = column[Option[Double]]("rate_max")
    def n_interviewing = column[Option[Int]]("n_interviewing")
    def interviewing_avg = column[Option[Double]]("interviewing_avg")
    def n_hires = column[Option[Int]]("n_hires")
    def client_registration_date = column[Option[Date]]("client_registration_date")
    def * = (job_id,create_date,last_viewed,n_applicants,applicants_avg,rate_min,rate_avg,rate_max,
      n_interviewing,interviewing_avg,n_hires)}
  private val jobsChangesTable = TableQuery[JobsChanges]
  private type ClientsChangesRowType = (Long,Date,Option[String],Option[Blob],Option[String],Option[String],
    Option[String],Option[Double],Option[Int],Option[String],Option[String],Option[Int],Option[Double],Option[Int],
    Option[Int],Option[Int],Option[Int],Option[Double],Option[Int],Option[Date])
  private class ClientsChanges(tag: Tag) extends Table[ClientsChangesRowType](tag, "odesk_clients_changes"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Date]("create_date", O.NotNull)
    def client_name = column[Option[String]]("client_name")
    def client_logo = column[Option[Blob]]("client_logo")
    def client_url = column[Option[String]]("client_url")
    def client_description = column[Option[String]]("client_description")
    def client_payment_method = column[Option[String]]("client_payment_method")
    def client_rating = column[Option[Double]]("client_rating")
    def client_n_reviews = column[Option[Int]]("client_n_reviews")
    def client_location = column[Option[String]]("client_location")
    def client_time = column[Option[String]]("client_time")
    def client_n_jobs = column[Option[Int]]("client_n_jobs")
    def client_hire_rate = column[Option[Double]]("client_hire_rate")
    def client_n_open_jobs = column[Option[Int]]("client_n_open_jobs")
    def client_total_spend = column[Option[Int]]("client_total_spend")
    def client_n_hires = column[Option[Int]]("client_n_hires")
    def client_n_active = column[Option[Int]]("client_n_active")
    def client_avg_rate = column[Option[Double]]("client_avg_rate")
    def client_hours = column[Option[Int]]("client_hours")
    def client_registration_date = column[Option[Date]]("client_registration_date")
    def * = (job_id,create_date,client_name,client_logo,client_url,client_description,
      client_payment_method,client_rating,client_n_reviews,client_location,client_time,client_n_jobs,
      client_hire_rate,client_n_open_jobs,client_total_spend,client_n_hires,client_n_active,
      client_avg_rate,client_hours,client_registration_date)}
  private val clientsChangesTable = TableQuery[ClientsChanges]
  private type JobsApplicantsRowType = (Long,Date,Option[Date],Option[String],Option[String],Option[String],Option[Long])
  private class JobsApplicants(tag: Tag) extends Table[JobsApplicantsRowType](tag, "odesk_jobs_applicants"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Date]("create_date", O.NotNull)
    def up_date = column[Option[Date]]("up_date")
    def name = column[Option[String]]("name")
    def initiated_by = column[Option[String]]("initiated_by")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (job_id,create_date,up_date,name,initiated_by,freelancer_url,freelancer_id)}
  private val jobsApplicantsTable = TableQuery[JobsApplicants]
  private type JobsHiredRowType = (Long,Date,Option[String],Option[String],Option[Long])
  private class JobsHired(tag: Tag) extends Table[JobsHiredRowType](tag, "odesk_jobs_hired"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Date]("create_date", O.NotNull)
    def name = column[Option[String]]("name")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (job_id,create_date,name,freelancer_url,freelancer_id)}
  private val jobsHiredTable = TableQuery[JobsHired]
  private type ClientsWorksHistoryRowType = (Long,Date,Option[String],Option[String],Option[String],Option[Date],Option[Date],
    Option[String],Option[Double],Option[Int],Option[Double],Option[String],Option[Double],
    Option[String],Option[String],Option[Long],Option[Double])
  private class ClientsWorksHistor(tag: Tag) extends Table[ClientsWorksHistoryRowType](tag, "odesk_clients_works_history"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def job_id = column[Long]("job_id", O.NotNull)
    def create_date = column[Date]("create_date", O.NotNull)
    def o_url = column[Option[String]]("o_url")
    def title = column[Option[String]]("title")
    def in_progress = column[Option[String]]("in_progress")
    def start_date = column[Option[Date]]("start_date")
    def end_date = column[Option[Date]]("end_date")
    def payment_type = column[Option[String]]("payment_type")
    def billed = column[Option[Double]]("billed")
    def hours = column[Option[Int]]("hours")
    def rate = column[Option[Double]]("rate")
    def freelancer_feedback_text = column[Option[String]]("freelancer_feedback_text")
    def freelancer_feedback = column[Option[Double]]("freelancer_feedback")
    def freelancer_name = column[Option[String]]("freelancer_name")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def client_feedback = column[Option[Double]]("client_feedback")
    def * = (job_id,create_date,o_url,title,in_progress,start_date,end_date,payment_type,billed,hours,rate,
      freelancer_feedback_text,freelancer_feedback,freelancer_name,freelancer_url,freelancer_id,client_feedback)}
  private val clientsWorksHistoryTable = TableQuery[ClientsWorksHistor]
  private type FoundFreelancersRowType = (String,Date,Int)
  private class FoundFreelancers(tag: Tag) extends Table[FoundFreelancersRowType](tag, "odesk_found_freelancers"){
      def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
      def o_url = column[String]("o_url", O.NotNull)
      def create_date = column[Date]("create_date", O.NotNull)
      def priority = column[Int]("priority", O.NotNull)
      def * = (o_url,create_date,priority)}
  private val foundFreelancersTable = TableQuery[FoundFreelancers]
  //Variables
  private var db:Option[DatabaseDef] = None
  //Service methods
  def init(url:String, user:String, password:String, dbName:String) = {
    val b = Database.forURL(url + "/" + dbName, driver = "scala.slick.driver.MySQLDriver", user = user, password = password)
    b.withSession(implicit session => {


      SET GLOBAL SQL_MODE=ANSI_QUOTES;

    val query = sql "select ID, NAME, AGE from PERSON".as[(Int,String,Int)]


    })

    db = Some(b)}
  def halt() ={
    db = None}
  //Data methods
  def addLogMessage(date:Date, name:String, msg:String):Long = {
    if(db.isEmpty){throw new Exception("[DBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {excavatorsLogTable += (date, name, msg)})}
  def addFoundJobsRow(d:FoundJobsRow) = {



  }

  def addJobsRow(d:JobsRow) = {



  }
  def addJobsChangesRow(d:JobsChangesRow) = {



  }
  def addClientsChangesRow(d:ClientsChangesRow) = {



  }
  def addJobsApplicantsRow(d:JobsApplicantsRow) = {



  }
  def addJobsHiredRow(d:JobsHiredRow) = {



  }
  def addClientsWorksHistoryRow(d:ClientsWorksHistoryRow) = {



  }



  def addFoundFreelancerRow(d:FoundFreelancerRow) = {



  }



















}

