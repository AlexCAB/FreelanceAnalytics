package excavators.odesk.db
import  java.sql.Date
import slick.driver.H2Driver.simple._
import slick.driver.H2Driver.backend.DatabaseDef

/**
 * Provide DB access for another components
 * Created by WORK on 22.09.14.
 */

class DBProvider {
  //Parameters
  val loggingTableName  = "excavators_log"
  val foundJobsTableName  = "odesk_found_jobs"
  val jobsTableName  = "odesk_jobs"
  val jobsChangesTableName  = "odesk_jobs_changes"
  val jobsApplicantsTableName  = "odesk_jobs_applicants"
  val jobsHiredTableName  = "odesk_jobs_hired"
  val clientsWorksHistoryTableName  = "odesk_clients_works_history"
  //Schema
  type logRow = (Date,String,String)
  class ExcavatorsLog(tag: Tag) extends Table[logRow](tag, "excavators_log"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def create_date = column[Date]("create_date", O.NotNull)
    def name = column[String]("name", O.NotNull)
    def msg = column[String]("msg")
    def * = (create_date, name, msg)}
  val excavatorsLogTable = TableQuery[ExcavatorsLog]


  odesk_jobs

  id
  o_url
  found_by
  found_date
  create_date
  post_date
  deadline
  dae_date
  delete_date
  next_check_date
  n_freelancers
  job_title
  job_tipe
  job_payment_type
  job_price
  job_employment
  jobt_length
  job_required_level
  job_skills
  job_qualifications
  job_description



  odesk_jobs_changes

  id
  job_id
  create_date
  last_viewed
  n_applicants
  applicants_avg
  rate_min
  rate_avg
  rate_max
  interviewing
  interviewing_avg
  n_hires
  client_name
  client_logo
  client_url
  client_description
  client_payment_method
  client_rating
  client_n_reviews
  client_location
  client_time
  client_n_jobs
  client_hire_rate
  client_n_open_jobs
  client_total_spend
  client_n_hires
  client_n_active
  client_avg_rate
  client_hours
  client_registration_date

  odesk_jobs_applicants


  id
  job_id
  create_date
  up_date
  name
  initiated_by
  freelancer_ur
  freelancer_id


  odesk_jobs_hired":


    id
    job_id
    create_date
    name
    freelancer_url
    freelancer_id


    odesk_clients_works_history


    id
    job_id
    create_date
    o_url
    title
    in_progress
    start_date
    end_date
    payment_type
    billed
    hours
    rate
    freelancer_feedback_text
    freelancer_feedback
    freelancer_name
    freelancer_url
    freelancer_id
    client_feedback





    //  //Variables
  var db:Option[DatabaseDef] = None
  //Service methods
  def init(url:String, user:String, password:String, dbName:String) = {
    db = Some(Database.forURL(url + "/" + dbName, driver = "scala.slick.driver.MySQLDriver", user = user, password = password))}
  def halt() ={
    db = None}
  //Data methods
  def addLogMessage(date:Date, name:String, msg:String):Long = {
    if(db.isEmpty){throw new Exception("[DBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {excavatorsLogTable += (date, name, msg)})}







}

//
//
//
//  def getClientAuthorizationData(clientKey:String):Option[VisitorAuthorizationData] = {
//    val st = con.createStatement()
//    try{
//      val r = st.executeQuery(selectForAuthorizationData(clientKey))
//      if(r.next()){
//        Some(VisitorAuthorizationData(
//          client = r.getInt("client_id"),
//          agentVersions = r.getString("agent_versions").split(",").toList))}
//      else{
//        None}}
//    finally{
//      st.close()}}
//  def getAgentProxyParameters(client:Int):AgentProxyParameters = {
//    val st = con.createStatement()
//    try{
//      val r = st.executeQuery(selectAgentProxyParameters(client.toString))
//      r.next()
//      AgentProxyParameters(
//        visitorLAs = r.getString("visitor_logic_actors").split(",").toList,
//        sessionCloseTimeout = r.getInt("session_stop_timeout"),
//        proxyEndWorkTimeout = r.getInt("proxy_stop_timeout"))}
//    finally{st.close()}}
//  def close() = {
//    con.close()}
//  //SQLs
//  def selectForAuthorizationData(ck:String):String = "select client_id,agent_versions from " + clientTableName + " where client_key = '" + ck + "';"
//  def selectAgentProxyParameters(cu:String):String = "select visitor_logic_actors,session_stop_timeout,proxy_stop_timeout from " + clientTableName + " where client_id = '" + cu + "';"}
