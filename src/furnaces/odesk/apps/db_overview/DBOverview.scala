package furnaces.odesk.apps.db_overview

import java.text.SimpleDateFormat
import java.util.Date

import furnaces.odesk.db.ODeskFurnacesDBProvider
import util.structures.FoundBy

/**
 * Tool for overview of result of excavator works
 * Created by CAB on 29.10.2014.
 */
object DBOverview {def main(args:Array[String]) = {
  //Helpers
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  implicit class IntForm(i:Int){
    def toPercentOfAsString(n:Int):String ={
      "(" + ((i.toDouble / n) * 100).toString.toSize(7).dropRightSpaces + "%)"}}
  implicit class StringForm(s:String){
    def toSize(n:Int):String = s match{
      case t if t.size > n => s.dropRight(s.size - n)
      case t if t.size < n => {s + (1 to (n - t.size)).map(_ => " ").mkString("")}
      case _ => s}
    def dropRightSpaces:String = s.reverse.dropWhile(_ == ' ').reverse}
  implicit class ListForm(ls:List[String]){
    def toMonoSize:List[String] = ls match {
      case l if l.nonEmpty => {
        val ms = ls.maxBy(_.size).size
        l.map(_.toSize(ms))}
      case _ => List()}}
  implicit class ParamsForm(ls:List[(String,String)]){
    def formParam:List[String] = {
      val (ks,vs) = ls.unzip
      ks.toMonoSize.zip(vs.toMonoSize).map{case(k,v) => {k + " = " + v}}}}
  implicit class ParamsFormWithPercent(ls:List[(String,Int)]){
    def formParamWithPercentOf(n:Int):List[String] = {
      val (ks,vs) = ls.unzip
      ks.toMonoSize.zip(vs.map(_.toString).toMonoSize).zip(vs).map{case((k,fv),v) => {k + " = " + fv + " " + v.toPercentOfAsString(n)}}}}
  //Get arguments
  if(args.size < 4){throw new Exception("Not enough arguments.")}
  val hostName = args(0)
  val userName = args(1)
  val passwordName = args(2)
  val dbName = args(3)
  val startDate = if(args.size < 5){None}else{Some(dateFormat.parse(args(4)))}
  val endDate = if(args.size < 6){None}else{Some(dateFormat.parse(args(5)))}
  //Connect to DB
  val db = new ODeskFurnacesDBProvider
  println("Connect to: " + hostName +  ", user: " + userName + ", password: " + passwordName + ", DB: " + dbName)
  db.init(hostName, userName, passwordName, dbName)
  //Table 'odesk_job_excavators_param'
  println("################ TABLE 'odesk_job_excavators_param' #################")
  val numberOfParameters = db.countRowsBethInExcavatorsParamTable(None,None)
  val activeParameters = db.loadExcavatorsActiveParameters
  println("Total number of params = " + numberOfParameters + ", number of active = " + activeParameters.size)
  println("Active parameters:")
  activeParameters.map(e => (e._1, e._2)).formParam.foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_excavators_log'
  println("################ TABLE 'odesk_excavators_log' #######################")
  val counts = db.countExcavatorsLogRows(startDate, endDate)
  println("Total number of messages = " + db.countRowsBethInExcavatorsLogTable(startDate, endDate))
  println("Number of messages by component:")
  counts.foreach{case (kn, ns) => {
    println(" Component '" + kn + "': infos = " + ns("info") + ", debugs = " + ns("debug") +
      ", worns = " + ns("worn") + ", errors = " + ns("error"))}}
  println("\n")
  //Table 'odesk_excavators_error_pages'
  println("################ TABLE 'odesk_excavators_error_pages' ###############")
  println("Total number of error parsed page = " + db.countRowsBethInParsingErrorsTable(startDate, endDate))
  println("\n")
  //Table 'odesk_found_jobs'
  println("################ TABLE 'odesk_found_jobs' ###########################")
  println("Total number of found job = " + db.countRowsBethInFoundJobsTable(startDate, endDate))
  val foundByCount = db.countFoundByJobsBeth(startDate, endDate)
  println("Number job found by search = " + foundByCount(FoundBy.Search) +
    ", by analyse = " + foundByCount(FoundBy.Analyse) +
    ", by unknown = " + foundByCount(FoundBy.Unknown))
  val byPriorityCount = db.countFoundByToScrapPriority(None, None)
  println("Number job found group by priorityColumn:")
  byPriorityCount.map{case(k,v) => (k.toString, v.toString)}.toList.formParam.foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_jobs'
  println("################ TABLE 'odesk_jobs' #################################")
  val (numberOfJobs, numberOfClose) = db.countNumberOfAllAndClosedJobsBeth(startDate, endDate)
  println("Total number of job = " + numberOfJobs)
  println("Number of closed job = " + numberOfClose + " " + numberOfClose.toPercentOfAsString(numberOfJobs))
  val jobColumnsOccupancy = db.jobColumnsOccupancy(startDate, endDate)
  println("Column occupancy:")
  jobColumnsOccupancy.formParamWithPercentOf(numberOfJobs).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_jobs_changes'
  println("################ TABLE 'odesk_jobs_changes' #########################")
  val numberOfJobChanges = db.countRowsBethInJobsChangesTable(startDate, endDate)
  val jobChangesColumnOccupancy = db.jobChangesColumnsOccupancy(startDate, endDate)
  println("Total number of job change row = " + numberOfJobChanges)
  println("Column occupancy:")
  jobChangesColumnOccupancy.formParamWithPercentOf(numberOfJobChanges).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_clients_changes'
  println("################ TABLE 'odesk_clients_changes' ######################")
  val numberOfClientsChanges = db.countRowsBethInClientChangesTable(startDate, endDate)
  val clientsChangesColumnOccupancy = db.clientChangesColumnsOccupancy(startDate, endDate)
  println("Total number of client changes row = " + numberOfClientsChanges)
  println("Column occupancy:")
  clientsChangesColumnOccupancy.formParamWithPercentOf(numberOfClientsChanges).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_jobs_applicants'
  println("################ TABLE 'odesk_jobs_applicants' ######################")
  val numberOfApplicants = db.countRowsBethInJobsApplicantsTable(startDate, endDate)
  val applicantsColumnOccupancy = db.applicantsColumnsOccupancy(startDate, endDate)
  println("Total number of applicants row = " + numberOfApplicants)
  println("Column occupancy:")
  applicantsColumnOccupancy.formParamWithPercentOf(numberOfApplicants).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_jobs_hired'
  println("################ TABLE 'odesk_jobs_hired' ###########################")
  val numberOfHired = db.countRowsBethInJobsHiredTable(startDate, endDate)
  val hiredColumnOccupancy = db.hiredColumnsOccupancy(startDate, endDate)
  println("Total number of hired row = " + numberOfHired)
  println("Column occupancy:")
  hiredColumnOccupancy.formParamWithPercentOf(numberOfHired).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_clients_works_history'
  println("################ TABLE 'odesk_clients_works_history' ################")
  val numberOfWorksHistory = db.countRowsBethInClientsWorksHistoryTable(startDate, endDate)
  val worksHistoryColumnOccupancy = db.worksHistoryColumnsOccupancy(startDate, endDate)
  println("Total number of works history row = " + numberOfWorksHistory)
  println("Column occupancy:")
  worksHistoryColumnOccupancy.formParamWithPercentOf(numberOfWorksHistory).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_found_freelancers'
  println("################ TABLE 'odesk_found_freelancers' ####################")
  val totalNumFoundFreelancers =  db.countRowsBethInFoundFreelancersTable(startDate, endDate)
  println("Total number of found freelancers = " + totalNumFoundFreelancers)
  println("\n")}}
