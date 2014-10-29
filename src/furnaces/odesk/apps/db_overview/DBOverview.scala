package furnaces.odesk.apps.db_overview

import java.text.SimpleDateFormat

import furnaces.odesk.db.ODeskFurnacesDBProvider

/**
 * Tool for overview of result of excavator works
 * Created by CAB on 29.10.2014.
 */
object DBOverview {def main(args:Array[String]) = {
  //Helpers
  val dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss")
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
  val numberOfParameters = db.countRows("odesk_job_excavators_param")
  val activeParameters = db.loadExcavatorsActiveParameters
  println("Total number of params = " + numberOfParameters + ", number of active = " + activeParameters.size)
  println("Active parameters:")
  activeParameters.map(e => (e._1, e._2)).formParam.foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_excavators_log'
  println("################ TABLE 'odesk_excavators_log' #######################")

  //Не забыть про даты

  val massages = List(("axc", "worn"),("axc2", "error"))                                                                 //<===
  println("Total number of messages = " + massages.size)
  println("Number of messages by component:")
  massages.map(_._1).toSet[String].foreach(kn => {
    val ni = massages.count(m => {m._1 == kn && m._2 == "info"})
    val nd = massages.count(m => {m._1 == kn && m._2 == "debug"})
    val nw = massages.count(m => {m._1 == kn && m._2 == "worn"})
    val ne = massages.count(m => {m._1 == kn && m._2 == "error"})
    println(" Component '" + kn + "': infos = " + ni + ", debugs = " + nd + ", worns = " + nw + ", errors = " + ne)})
  println("\n")
  //Table 'odesk_excavators_error_pages'
  println("################ TABLE 'odesk_excavators_error_pages' ###############")
  val numberOfErrorPage = 10                                                                                             //<===
  println("Total number of error parsed page = " + numberOfErrorPage)
  println("\n")
  //Table 'odesk_found_jobs'
  println("################ TABLE 'odesk_found_jobs' ###########################")
  val (totalNumFoundJobs,bySearchNumFoundJobs,byAnalyseNumFoundJobs) = (1,2,3)                                           //<===
  println("Total number of found job = " + totalNumFoundJobs)
  println("Number found by search = " + bySearchNumFoundJobs + ", by analyse = " + byAnalyseNumFoundJobs)
  println("\n")
  //Table 'odesk_jobs'
  println("################ TABLE 'odesk_jobs' #################################")
  val (numberOfJobs, numberOfClose, jobColumnOccupancy) = (10, 8, List(("col1",5),("col2",2)))                           //<===
  println("Total number of job = " + numberOfJobs)
  println("Number of closed job = " + numberOfClose + " " + numberOfClose.toPercentOfAsString(numberOfJobs))
  println("Column occupancy:")
  jobColumnOccupancy.formParamWithPercentOf(numberOfJobs).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_jobs_changes'
  println("################ TABLE 'odesk_jobs_changes' #########################")
  val (numberOfJobChanges, jobChangesColumnOccupancy) = (10, List(("col1",5),("col2",2)))                                //<===
  println("Total number of job change row = " + numberOfJobChanges)
  println("Column occupancy:")
  jobChangesColumnOccupancy.formParamWithPercentOf(numberOfJobChanges).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_clients_changes'
  println("################ TABLE 'odesk_clients_changes' ######################")
  val (numberOfClientsChanges, clientsChangesColumnOccupancy) = (10, List(("col1",5),("col2",2)))                        //<===
  println("Total number of client changes row = " + numberOfClientsChanges)
  println("Column occupancy:")
  clientsChangesColumnOccupancy.formParamWithPercentOf(numberOfClientsChanges).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_jobs_applicants'
  println("################ TABLE 'odesk_jobs_applicants' ######################")
  val (numberOfApplicants, applicantsColumnOccupancy) = (10, List(("col1",5),("col2",2)))                                //<===
  println("Total number of applicants row = " + numberOfApplicants)
  println("Column occupancy:")
  applicantsColumnOccupancy.formParamWithPercentOf(numberOfApplicants).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_jobs_hired'
  println("################ TABLE 'odesk_jobs_hired' ###########################")
  val (numberOfHired, hiredColumnOccupancy) = (10, List(("col1",5),("col2",2)))                                          //<===
  println("Total number of hired row = " + numberOfHired)
  println("Column occupancy:")
  hiredColumnOccupancy.formParamWithPercentOf(numberOfHired).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_clients_works_history'
  println("################ TABLE 'odesk_clients_works_history' ################")
  val (numberOfWorksHistory, worksHistoryColumnOccupancy) = (10, List(("col1",5),("col2",2)))                            //<===
  println("Total number of works history row = " + numberOfWorksHistory)
  println("Column occupancy:")
  worksHistoryColumnOccupancy.formParamWithPercentOf(numberOfWorksHistory).foreach(e => println("  " + e))
  println("\n")
  //Table 'odesk_found_freelancers'
  println("################ TABLE 'odesk_found_freelancers' ####################")
  val totalNumFoundFreelancers = 10                                                                                      //<===
  println("Total number of found freelancers = " + totalNumFoundFreelancers)
  println("\n")}}
