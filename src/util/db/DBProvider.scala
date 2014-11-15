package util.db

import java.io.ByteArrayOutputStream
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import javax.imageio.ImageIO
import util.logging.LoggerDBProvider
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
  protected val urlColumn = "o_url"
  protected val jobIdColumn = "job_id"
  protected val idColumn = "id"
  protected val logTypesNames = List("info","debug","worn","error")
  protected val jobNullableColumns = List(
    "post_date","deadline",daeDateColumn,"delete_date","next_check_date","n_freelancers",
    "job_title","job_type","job_price","job_length","job_description")
  protected val jobUnknowableColumns = List("job_payment_type","job_employment","job_required_level",foundByColumn)
  protected val jobLists = List("job_skills","job_qualifications")
  protected val jobChangeNullableColumns = List("last_viewed","n_applicants","applicants_avg","rate_min",
    "rate_avg","rate_max","n_interviewing","interviewing_avg","n_hires")
  protected val jobChangeUnknowableColumns = List("available")
  protected val clientsChangesNullableColumns = List(nameColumn,"logo","url","description","rating","n_reviews",
    "location","time","n_jobs","hire_rate","n_open_jobs","total_spend","n_hires","n_active",
    "avg_rate","hours","registration_date")
  protected val clientsChangesUnknowableColumns = List("payment_method")
  protected val applicantsNullableColumns = List("up_date",nameColumn,"freelancer_url","freelancer_id")
  protected val applicantsUnknowableColumns = List("initiated_by")
  protected val hiredNullableColumns = List(nameColumn,"freelancer_url","freelancer_id")
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
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def p_key = column[String]("p_key", O.NotNull)
    def p_value = column[String]("p_value", O.NotNull)
    def is_active = column[Boolean]("is_active", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def comment = column[Option[String]]("comment")
    def * = (id,p_key,p_value,is_active,create_date,comment)}
  protected val excavatorsParamTable = TableQuery[ExcavatorsParam]
  protected type LogRowType = (Option[Long],Timestamp,String,String,String)
  protected class ExcavatorsLog(tag: Tag) extends Table[LogRowType](tag, odesk_excavators_log){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def mType = column[String](typeColumn, O.NotNull)
    def name = column[String](nameColumn, O.NotNull)
    def msg = column[String]("msg")
    def * = (id,create_date,mType, name, msg)}
  protected val excavatorsLogTable = TableQuery[ExcavatorsLog]
  protected type ParsingErrorsRowType = (Option[Long],Timestamp,String,String,String)
  protected class ParsingErrors(tag: Tag) extends Table[ParsingErrorsRowType](tag, odesk_excavators_error_pages){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def o_url = column[String]("o_url", O.NotNull)
    def msg = column[String]("msg", O.NotNull)
    def html = column[String]("html", O.NotNull)
    def * = (id,create_date,o_url,msg,html)}
  protected val parsingErrorsTable = TableQuery[ParsingErrors]
  protected type FoundJobsRowType = (Option[Long],String,String,Timestamp,Int,String,Option[Int])
  protected class FoundJobs(tag: Tag) extends Table[FoundJobsRowType](tag, odesk_found_jobs){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def o_url = column[String]("o_url", O.NotNull)
    def found_by = column[String](foundByColumn, O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def priority = column[Int](priorityColumn, O.NotNull)
    def job_skills = column[String]("job_skills", O.NotNull)
    def n_freelancers = column[Option[Int]]("n_freelancers")
    def * = (id,o_url,found_by,create_date,priority,job_skills,n_freelancers)}
  protected val foundJobsTable = TableQuery[FoundJobs]
  protected type JobRowType = (Option[Long],String,String,Timestamp,Timestamp,Option[Timestamp],
    Option[Timestamp],Option[Timestamp],Option[Timestamp],
    Option[Timestamp],Option[Int],Option[String],Option[String],String,Option[Double],String,
    Option[String],String,String,String,Option[String])
  protected class Jobs(tag: Tag) extends Table[JobRowType](tag, odesk_jobs){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def o_url = column[String]("o_url", O.NotNull)
    def found_by = column[String](foundByColumn, O.NotNull)
    def found_date = column[Timestamp]("found_date", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def post_date = column[Option[Timestamp]]("post_date")
    def deadline = column[Option[Timestamp]]("deadline")
    def dae_date = column[Option[Timestamp]](daeDateColumn)
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
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def job_id = column[Long](jobIdColumn, O.NotNull)
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
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def job_id = column[Long](jobIdColumn, O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def name = column[Option[String]](nameColumn)
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
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def job_id = column[Long](jobIdColumn, O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def up_date = column[Option[Timestamp]]("up_date")
    def name = column[Option[String]](nameColumn)
    def initiated_by = column[String]("initiated_by")
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (id,job_id,create_date,up_date,name,initiated_by,freelancer_url,freelancer_id)}
  protected val jobsApplicantsTable = TableQuery[JobsApplicants]
  protected type JobsHiredRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],Option[Long])
  protected class JobsHired(tag: Tag) extends Table[JobsHiredRowType](tag, odesk_jobs_hired){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def job_id = column[Long](jobIdColumn, O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def name = column[Option[String]](nameColumn)
    def freelancer_url = column[Option[String]]("freelancer_url")
    def freelancer_id = column[Option[Long]]("freelancer_id")
    def * = (id,job_id,create_date,name,freelancer_url,freelancer_id)}
  protected val jobsHiredTable = TableQuery[JobsHired]
  protected type ClientsWorksHistoryRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],String,Option[Timestamp],Option[Timestamp],
    String,Option[Double],Option[Int],Option[Double],Option[String],Option[Double],
    Option[String],Option[String],Option[Long],Option[Double])
  protected class ClientsWorksHistor(tag: Tag) extends Table[ClientsWorksHistoryRowType](tag, odesk_clients_works_history){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def job_id = column[Long](jobIdColumn, O.NotNull)
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
      def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
      def o_url = column[String]("o_url", O.NotNull)
      def create_date = column[Timestamp]("create_date", O.NotNull)
      def priority = column[Int](priorityColumn, O.NotNull)
      def * = (id,o_url,create_date,priority)}
  protected val foundFreelancersTable = TableQuery[FoundFreelancers]





  protected type FreelancersRowType = (Option[Long],Timestamp,String)
  protected class Freelancers (tag: Tag) extends Table[FreelancersRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def o_url = column[String]("o_url", O.NotNull)
    def * = (id,create_date,o_url)}
  protected val freelancersTable = TableQuery[Freelancers]
  protected type FreelancersRawHtmlRowType = (Option[Long],Long,Timestamp,String)
  protected class FreelancersRawHtml (tag: Tag) extends Table[FreelancersRawHtmlRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def html = column[String]("html", O.NotNull)
    def * = (id,freelancer_id,create_date,html)}
  protected val freelancersRawHtmlTable = TableQuery[FreelancersRawHtml]
  protected type FreelancersRawJobJsonRowType = (Option[Long],Long,Timestamp,String)
  protected class FreelancersRawJob (tag: Tag) extends Table[FreelancersRawJobJsonRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def json = column[String]("json", O.NotNull)
    def * = (id,freelancer_id,create_date,json)}
  protected val freelancersRawJobTable = TableQuery[FreelancersRawJob]
  protected type FreelancersRawPortfolioRowType = (Option[Long],Long,Timestamp,String)
  protected class FreelancersRawPortfolio (tag: Tag) extends Table[FreelancersRawPortfolioRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def json = column[String]("json", O.NotNull)
    def * = (id,freelancer_id,create_date,json)}
  protected val freelancersRawPortfolioTable = TableQuery[FreelancersRawPortfolio]
  protected type FreelancersMainChangeRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],
    Option[String],Option[String],Option[String],Option[String],String,Option[String],Option[Int],String,
    Option[Array[Byte]],Option[String],Option[String])
  protected class FreelancersMainChange(tag: Tag) extends Table[FreelancersMainChangeRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def name = column[Option[String]]("name")
    def profile_access = column[Option[String]]("profile_access")
    def link = column[Option[String]]("link")
    def expose_full_name = column[Option[String]]("expose_full_name")
    def role = column[Option[String]]("role")
    def video_url = column[Option[String]]("video_url")
    def is_invite_interview_allowed = column[String]("is_invite_interview_allowed", O.NotNull)
    def location = column[Option[String]]("location")
    def time_zone = column[Option[Int]]("time_zone")
    def email_verified = column[String]("email_verified", O.NotNull)
    def photo = column[Option[Array[Byte]]]("photo")
    def company_url = column[Option[String]]("company_url")
    def company_logo = column[Option[String]]("company_logo")
    def * = (id,freelancer_id,create_date,name,profile_access,link,expose_full_name,role,video_url,
      is_invite_interview_allowed,location,time_zone,email_verified,photo,company_url,company_logo)}
  protected val freelancersMainChangeTable = TableQuery[FreelancersMainChange]
  protected type FreelancersAdditionalChangeRowType = (Option[Long],Long,Timestamp,Option[String],String,Option[String],
    Option[String],Option[String],String,Option[Double],Option[Int],Option[Double],Option[Int],Option[Int],String)
  protected class FreelancersAdditionalChange (tag: Tag) extends Table[FreelancersAdditionalChangeRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def title = column[Option[String]]("title")
    def availability = column[String]("availability", O.NotNull))
    def available_again = column[Option[String]]("available_again")
    def responsiveness_score = column[Option[String]]("responsiveness_score")
    def overview = column[Option[String]]("overview")
    def languages = column[String]("languages", O.NotNull)
    def rate = column[Option[Double]]("rate")
    def rent_percent = column[Option[Int]]("rent_percent")
    def rating = column[Option[Double]]("rating", O.NotNull)
    def all_time_jobs = column[Option[Int]]("all_time_jobs")
    def all_time_hours = column[Option[Int]]("all_time_hours")
    def skills = column[String]("skills", O.NotNull)
    def * = (id,freelancer_id,create_date,title,availability,available_again,responsiveness_score,overview,
      languages,rate,rent_percent,rating,all_time_jobs,all_time_hours,skills)}
  protected val freelancersAdditionalChangeTable = TableQuery[FreelancersAdditionalChange]
  protected type FreelancersWorkRowType = (Option[Long],Long,Timestamp,String,String,Option[Timestamp],Option[Timestamp],
    Option[Timestamp],Option[Timestamp],Option[String],Option[String],
    String,String,
    Option[String],Option[String],String,Option[String],String)
  protected class FreelancersWork (tag: Tag) extends Table[FreelancersWorkRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def payment_type = column[String]("payment_type", O.NotNull)
    def status = column[String]("status", O.NotNull)
    def start_date = column[Option[Timestamp]]("start_date")
    def end_date = column[Option[Timestamp]]("end_date")
    def from_full = column[Option[Timestamp]]("from_full")
    def to_full = column[Option[Timestamp]]("to_full")
    def opening_title = column[Option[String]]("opening_title")
    def engagement_title = column[Option[String]]("opening_title")
    def skills = column[String]("skills", O.NotNull)
    def open_access = column[String]("open_access", O.NotNull)
    def cny_status = column[Option[String]]("cny_status")
    def financial_privacy = column[Option[String]]("financial_privacy")
    def is_hidden = column[String]("is_hidden", O.NotNull)
    def agency_name = column[Option[String]]("agency_name")
    def segmentation_data = column[String]("segmentation_data", O.NotNull)
    def * = (id,freelancer_id,create_date,payment_type,status,start_date,end_date,from_full,to_full,engagement_title,
      opening_title,skills,open_access,cny_status,financial_privacy,is_hidden,agency_name,segmentation_data)}
  protected val freelancersWorkTable = TableQuery[FreelancersWork]
  protected type FreelancersWorkAdditionalDataRowType = (Option[Long],Long,Long,Timestamp,
    Option[String],Option[Int],Option[Double],Option[Double],Option[Double],Option[Double],Option[Double],
    Option[Double],Option[Double],Option[Int],Option[String], Option[String],Option[String],Option[String],
    Option[String],Option[Double])
  protected class FreelancersWorkAdditionalData (tag: Tag) extends Table[FreelancersWorkAdditionalDataRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def work_id = column[Long]("work_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def as_type = column[Option[String]]("as_type")
    def total_hours = column[Option[Int]]("total_hours")
    def rate = column[Option[Double]]("rate")
    def total_cost = column[Option[Double]]("total_cost")
    def charge_rate = column[Option[Double]]("charge_rate")
    def amount = column[Option[Double]]("amount")
    def total_hours_precise = column[Option[Double]]("total_hours_precise")
    def cost_rate = column[Option[Double]]("cost_rate")
    def total_charge = column[Option[Double]]("total_charge")
    def job_contractor_tier = column[Option[Int]]("job_contractor_tier")
    def job_url = column[Option[String]]("job_url")
    def job_description = column[Option[String]]("job_description")
    def job_category = column[Option[String]]("job_category")
    def job_engagement = column[Option[String]]("job_engagement")
    def job_duration = column[Option[String]]("job_duration")
    def job_amount = column[Option[Double]]("job_amount")
    def * = (id,freelancer_id,work_id,create_date,as_type,total_hours,rate,total_cost,charge_rate,amount,total_hours_precise,
      cost_rate,total_charge,job_contractor_tier,job_url,job_description,job_category,job_engagement,job_duration,job_amount)}
  protected val freelancersWorkAdditionalDataTable = TableQuery[FreelancersWorkAdditionalData]
  protected type FreelancersWorkFeedbackRowType = (Option[Long],Long,Long,Timestamp,String,String,Option[String],
    Option[Int],String,Option[String],Option[Double],String,String,Option[String],Option[String],Option[Double])
  protected class FreelancersWorkFeedback(tag: Tag) extends Table[FreelancersWorkFeedbackRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def work_id = column[Long]("work_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def ff_scores = column[String]("ff_scores", O.NotNull)
    def ff_is_public = column[String]("ff_is_public", O.NotNull)
    def ff_comment = column[Option[String]]("ff_comment")
    def ff_private_point = column[Option[Int]]("ff_private_point")
    def ff_reasons = column[String]("ff_reasons", O.NotNull)
    def ff_response = column[Option[String]]("ff_response")
    def ff_score = column[Option[Double]]("ff_score")
    def cf_scores = column[String]("cf_scores", O.NotNull)
    def cf_is_public = column[String]("cf_is_public", O.NotNull)
    def cf_comment = column[Option[String]]("cf_comment")
    def cf_response = column[Option[String]]("cf_response")
    def cf_score = column[Option[Double]]("cf_score")
    def * = (id,freelancer_id,work_id,create_date,ff_scores,ff_is_public,ff_comment,ff_private_point,ff_reasons,
      ff_response,ff_score,cf_scores,cf_is_public,cf_comment,cf_response,cf_score)}
  protected val freelancersWorkFeedbackTable = TableQuery[FreelancersWorkFeedback]
  protected type FreelancersWorkLinkedProjectDataRowType = (Option[Long],Long,Long,Timestamp,Option[String],Option[String],
    String,Option[String],Option[String],Option[String],Option[String],Option[String],Option[String],Option[String],Option[String])
  protected class FreelancersWorkLinkedProjectData(tag: Tag) extends Table[FreelancersWorkLinkedProjectDataRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def work_id = column[Long]("work_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def lp_title = column[Option[String]]("lp_title")
    def lp_thumbnail = column[Option[String]]("lp_thumbnail")
    def lp_is_public = column[String]("lp_is_public", O.NotNull)
    def lp_description = column[Option[String]]("lp_description")
    def lp_recno = column[Option[String]]("lp_recno")
    def lp_cat_level_1 = column[Option[String]]("lp_cat_level_1")
    def lp_cat_recno = column[Option[String]]("lp_cat_recno")
    def lp_cat_level_2 = column[Option[String]]("lp_cat_level_2")
    def lp_completed = column[Option[String]]("lp_completed")
    def lp_large_thumbnail = column[Option[String]]("lp_large_thumbnail")
    def lp_url = column[Option[String]]("lp_url")
    def lp_project_contract_link_state = column[Option[String]]("lp_project_contract_link_state")
    def * = (id,freelancer_id,work_id,create_date,lp_title,lp_thumbnail,lp_is_public,lp_description,
      lp_recno,lp_cat_level_1,lp_cat_recno,lp_completed,lp_large_thumbnail,lp_url,lp_project_contract_link_state)}
  protected val freelancersWorkLinkedProjectDataTable = TableQuery[FreelancersWorkLinkedProjectData]
  protected type FreelancersWorkClientsRowType = (Option[Long],Long,Long,Timestamp,Option[Int],Option[Double],Option[Double],
    Option[Int],Option[Int],Option[String],Option[String],Option[String],Option[Timestamp],Option[Array[Byte]],
    Option[String],Option[String],Option[String])
  protected class FreelancersWorkClients(tag: Tag) extends Table[FreelancersWorkClientsRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def work_id = column[Long]("work_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def client_total_feedback = column[Option[Int]]("client_total_feedback")
    def client_score = column[Option[Double]]("client_score")
    def client_total_charge = column[Option[Double]]("client_total_charge")
    def client_total_hires = column[Option[Int]]("client_total_hires")
    def client_active_contract = column[Option[Int]]("client_active_contract")
    def client_country = column[Option[String]]("client_country")
    def client_city = column[Option[String]]("client_city")
    def client_time = column[Option[String]]("client_time")
    def client_member_since = column[Option[Timestamp]]("client_member_since")
    def client_profile_logo = column[Option[Array[Byte]]]("client_profile_logo")
    def client_profile_name = column[Option[String]]("client_profile_name")
    def client_profile_url = column[Option[String]]("client_profile_url")
    def client_profile_summary = column[Option[String]]("client_profile_summary")
   def * = (id,freelancer_id,work_id,create_date,client_total_feedback,client_score,client_total_charge,
      client_total_hires,client_active_contract,client_country,client_city,client_time,client_member_since,
      client_profile_logo,client_profile_name,client_profile_url,client_profile_summary)}
  protected val freelancersWorkClientsTable = TableQuery[FreelancersWorkClients]
  protected type FreelancersPortfolioRowType = (Option[Long],Long,Timestamp,Option[Timestamp],String,Option[String],
    String,String,Option[Timestamp],Option[String],Option[String],String,String,Option[String],Option[String],Option[Array[Byte]])
  protected class FreelancersPortfolio(tag: Tag) extends Table[FreelancersPortfolioRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def project_date = column[Option[Timestamp]]("project_date")
    def title = column[String]("title")
    def description = column[Option[String]]("description")
    def is_public = column[String]("is_public")
    def attachments = column[String]("attachments")
    def creation_ts = column[Option[Timestamp]]("creation_ts")
    def category = column[Option[String]]("category")
    def sub_category = column[Option[String]]("sub_category")
    def skills = column[String]("skills")
    def is_client = column[String]("is_client")
    def flag_comment = column[Option[String]]("flag_comment")
    def project_url = column[Option[String]]("project_url")
    def img_url = column[Option[Array[Byte]]]("img_url")
    def * = (id,freelancer_id,create_date,project_date,title,description,is_public,attachments,creation_ts,category,
      sub_category,skills,is_client,flag_comment,project_url,img_url)}
  protected val freelancersPortfolioTable = TableQuery[FreelancersPortfolio]
  protected type FreelancersTestsRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],Option[Double],Option[Int])
  protected class FreelancersTests(tag: Tag) extends Table[FreelancersTestsRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def details_url = column[Option[String]]("details_url")
    def title = column[Option[String]]("title")
    def score = column[Option[Double]]("score")
    def time_complete = column[Option[Int]]("time_complete")
    def * = (id,freelancer_id,create_date,details_url,title,score,time_complete)}
  protected val freelancersTestsTable = TableQuery[FreelancersTests]
  protected type FreelancersCertificationRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],
    Option[String],Option[String],Option[String],Option[String],Option[String],String,String,Option[String],Option[String],
    String,Option[String])
  protected class FreelancersCertification(tag: Tag) extends Table[FreelancersCertificationRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def details_url = column[Option[String]]("details_url")
    def rid = column[Option[String]]("rid")
    def name = column[Option[String]]("name")
    def custom_data = column[Option[String]]("custom_data")
    def score = column[Option[String]]("score")
    def logo_url = column[Option[String]]("logo_url")
    def cert_url = column[Option[String]]("cert_url")
    def is_cert_verified = column[String]("is_cert_verified", O.NotNull)
    def is_verified = column[String]("is_verified", O.NotNull)
    def description = column[Option[String]]("description")
    def provider = column[Option[String]]("provider")
    def skills = column[String]("skills", O.NotNull)
    def date_earned = column[Option[String]]("date_earned")
    def * = (id,freelancer_id,create_date,details_url,rid,name,custom_data,score,logo_url,cert_url,is_cert_verified,
      is_verified,description,provider,skills,date_earned)}
  protected val freelancersCertificationTable = TableQuery[FreelancersCertification]
  protected type FreelancersEmploymentRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],
    Option[String],Option[String],Option[Timestamp],Option[Timestamp],Option[String],Option[String],Option[String],
    Option[String])
  protected class FreelancersEmployment(tag: Tag) extends Table[FreelancersEmploymentRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def details_url = column[Option[String]]("details_url")
    def record_id = column[Option[String]]("record_id")
    def title = column[Option[String]]("title")
    def company = column[Option[String]]("company")
    def date_from = column[Option[Timestamp]]("date_from")
    def date_to = column[Option[Timestamp]]("date_to")
    def role = column[Option[String]]("role")
    def company_country = column[Option[String]]("company_country")
    def company_city = column[Option[String]]("company_city")
    def description = column[Option[String]]("description")
    def * = (id,freelancer_id,create_date,details_url,record_id,title,company,date_from,date_to,role,company_country,
      company_city,description)}
  protected val freelancersEmploymentTable = TableQuery[FreelancersEmployment]
  protected type FreelancersEducationRowType = (Option[Long],Long,Timestamp,Option[String],Option[String],Option[String],
    Option[String],Option[Timestamp],Option[Timestamp],Option[String])
  protected class FreelancersEducation(tag: Tag) extends Table[FreelancersEducationRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def details_url = column[Option[String]]("details_url")
    def school = column[Option[String]]("school")
    def area_of_study = column[Option[String]]("area_of_study")
    def degree = column[Option[String]]("degree")
    def date_from = column[Option[Timestamp]]("date_from")
    def date_to = column[Option[Timestamp]]("date_to")
    def comments = column[Option[String]]("comments")
    def * = (id,freelancer_id,create_date,details_url,school,area_of_study,degree,date_from,date_to,comments)}
  protected val freelancersEducationTable = TableQuery[FreelancersEducation]
  protected type FreelancersOtherExperienceRowType = (Option[Long],Long,Timestamp,Option[String],Option[String])
  protected class FreelancersOtherExperience(tag: Tag) extends Table[FreelancersOtherExperienceRowType](tag, odesk_found_freelancers){
    def id = column[Option[Long]](idColumn,O.PrimaryKey, O.AutoInc)
    def freelancer_id = column[Long]("freelancer_id", O.NotNull)
    def create_date = column[Timestamp]("create_date", O.NotNull)
    def subject = column[Option[String]]("subject")
    def description = column[Option[String]]("description")
    def * = (id,freelancer_id,create_date,subject,description)}
  protected val freelancersOtherExperienceTable = TableQuery[FreelancersOtherExperience]
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
    d.nFreelancers)}               // n_freelancers
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


















  protected def buildFreelancersRowType:FreelancersRowType = (
    , // id
    , // create_date
    , // o_url
    )

  protected def buildFreelancersRawHtmlRowType(d:, fId:Long):FreelancersRawHtmlRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // html
    )


  protected def buildFreelancersRawJobJsonRowType(d:, fId:Long):FreelancersRawJobJsonRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // json
    )


  protected def buildFreelancersRawPortfolioRowType(d:, fId:Long):FreelancersRawPortfolioRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // json
    )


  protected def buildFreelancersMainChangeRowType(d:, fId:Long):FreelancersMainChangeRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // name
    , // profile_access
    , // link
    , // expose_full_name
    , // role
    , // video_url
    , // is_invite_interview_allowed
    , // location
    , // time_zone
    , // email_verified
    , // photo
    , // company_url
    , // company_logo
    )


  protected def buildFreelancersAdditionalChangeRowType(d:, fId:Long):FreelancersAdditionalChangeRowType = (
    , // id
    , // create_date
    , // freelancer_id
    , // title
    , // availability
    , // available_again
    , // responsiveness_score
    , // overview
    , // languages
    , // rate
    , // rent_percent
    , // rating
    , // all_time_jobs
    , // all_time_hours
    , // skills
    )


  protected def buildFreelancersWorkRowType(d:, fId:Long):FreelancersWorkRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // payment_type
    , // status
    , // start_date
    , // end_date =
    , // from_full
    , // to_full
    , // opening_title
    , // engagement_title
    , // skills
    , // open_access
    , // cny_status
    , // financial_privacy
    , // is_hidden
    , // agency_name
    , // segmentation_data
    )


  protected def buildFreelancersWorkAdditionalDataRowType(d:, fId:Long, wId:Long):FreelancersWorkAdditionalDataRowType = (
    , // id
    , // freelancer_id
    , // work_id
    , // create_date
    , // as_type
    , // total_hours
    , // rate
    , // total_cost
    , // charge_rate
    , // amount
    , // total_hours_precise
    , // cost_rate
    , // total_charge
    , // job_contractor_tier
    , // job_url
    , // job_description
    , // job_category
    , // job_engagement
    , // job_duration
    , // job_amount
    )


  protected def buildFreelancersWorkFeedbackRowType(d:, fId:Long, wId:Long):FreelancersWorkFeedbackRowType = (
    , // id
    , // freelancer_id
    , // work_id
    , // create_date
    , // ff_scores
    , // ff_is_public
    , // ff_comment
    , // ff_private_point
    , // ff_reasons
    , // ff_response
    , // ff_score
    , // cf_scores
    , // cf_is_public
    , // cf_comment
    , // cf_response
    , // cf_score
    )


  protected def buildFreelancersWorkLinkedProjectDataRowType(d:, fId:Long, wId:Long):FreelancersWorkLinkedProjectDataRowType = (
    , // id
    , // freelancer_id
    , // work_id
    , // create_date
    , // lp_title
    , // lp_thumbnail
    , // lp_is_public
    , // lp_description
    , // lp_recno
    , // lp_cat_level_1
    , // lp_cat_recno
    , // lp_cat_level_2
    , // lp_completed
    , // lp_large_thumbnail
    , // lp_url
    , // lp_project_contract_link_state
    )


  protected def buildFreelancersWorkClientsRowType(d:, fId:Long, wId:Long):FreelancersWorkClientsRowType = (
    , // id
    , // freelancer_id
    , // work_id
    , // create_date
    , // client_total_feedback
    , // client_score
    , // client_total_charge
    , // client_total_hires
    , // client_active_contract
    , // client_country
    , // client_city
    , // client_time
    , // client_member_since
    , // client_profile_logo
    , // client_profile_name
    , // client_profile_url
    , // client_profile_summary
    )


  protected def buildFreelancersPortfolioRowType(d:, fId:Long):FreelancersPortfolioRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // project_date
    , // title
    , // description
    , // is_public
    , // attachments
    , // creation_ts
    , // category
    , // sub_category
    , // skills
    , // is_client
    , // flag_comment
    , // project_url
    , // img_url
    )


  protected def buildFreelancersTestsRowType(d:, fId:Long):FreelancersTestsRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // details_url
    , // title
    , // score
    , // time_complete
    )


  protected def buildFreelancersCertificationRowType(d:, fId:Long):FreelancersCertificationRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // details_url
    , // rid
    , // name
    , // custom_data
    , // score
    , // logo_url
    , // cert_url
    , // is_cert_verified
    , // is_verified
    , // description
    , // provider
    , // skills
    , // date_earned
    )


  protected def buildFreelancersEmploymentRowType(d:, fId:Long):FreelancersEmploymentRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // details_url
    , // record_id
    , // title
    , // company
    , // date_from
    , // date_to
    , // role
    , // company_country
    , // company_city
    , // description
    )


  protected def buildFreelancersEducationRowType(d:, fId:Long):FreelancersEducationRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // details_url
    , // school
    , // area_of_study
    , // degree
    , // date_from
    , // date_to
    , // comments
    )


  protected def buildFreelancersOtherExperienceRowType(d:, fId:Long):FreelancersOtherExperienceRowType = (
    , // id
    , // freelancer_id
    , // create_date
    , // subject
    , // description
    )
















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
    db.get.withSession(implicit session => {excavatorsLogTable += (None, new Timestamp(date.getTime), mType, name, msg)})}
  def addFoundJobsRows(ds:List[FoundJobsRow]):Int = {  //Return number of insert(not insert if already exist in jobTable or in foundJobsTableTable)
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {
      //Filtering of exist
      val us = ds.map(_.oUrl)
      val ejs = (jobTable.filter(_.o_url inSetBind us).map(_.o_url) ++ foundJobsTable.filter(_.o_url inSetBind us).map(_.o_url)).list.toSet
      val fds = ds.filter(u => {! ejs.contains(u.oUrl)})
      //Prepare
      val rs = fds.map(d => buildFoundJobsRow(d,d.priority))               //n_freelancers
      //Insert
      foundJobsTable ++= rs
      //Return  number of insert
      fds.size})}




}
























































































