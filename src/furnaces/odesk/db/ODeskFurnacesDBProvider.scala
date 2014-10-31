package furnaces.odesk.db

import java.util.Date
import util.structures._
import util.db.DBProvider
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Provide DB access for oDesk excavators
 * Created by CAB on 29.10.2014.
 */

class ODeskFurnacesDBProvider extends DBProvider{
  //Functions
  private def calcOccupancy(tn:String, from:Option[Date], to:Option[Date], nullable:List[String],unknowable:List[String],lists:List[String]):List[(String,Int)] = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.calcOccupancy] No created DB.")}
    db.get.withSession(implicit session => {
      //Prepare
      val dp = buildDateCondition(from,to) match{case Some(p) => {" and " + p}; case None => ""}
      //Count
      val nnc = nullable.map(cn => {
        (cn, Q.queryNA[Int]("select count(*) from " + tn + " where " + cn + " is not null " + dp).first)})
      val nuc = unknowable.map(cn => {
        (cn, Q.queryNA[Int]("select count(*) from " + tn + " where " + cn + " <> 'Unknown' " + dp).first)})
      val lc = lists.map(cn => {
        (cn, Q.queryNA[Int]("select count(*) from " + tn + " where " + cn + " <> '' " + dp).first)})
      //Return
      nnc ++ nuc ++ lc})}
  private def countRowsBeth(tableName:String, from:Option[Date], to:Option[Date]):Int = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.countExcavatorsLogRows] No created DB.")}
    if(! tablesByName.contains(tableName)){throw new Exception("[DBProvider.countRowsBeth] Unknown table nameColumn: " + tableName)}
    db.get.withSession(implicit session => {
      //Prepare
      val dp = buildDateCondition(from,to) match{case Some(p) => {" where " + p}; case None => ""}
      //Count
      Q.queryNA[Int]("select count(*) from " + tableName + dp).first})}
  //Data methods
  def loadExcavatorsActiveParameters:List[(String,String)] = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {
      excavatorsParamTable.filter(_.is_active === true).map(r => (r.p_key, r.p_value)).list})}
  def countRowsBethInExcavatorsParamTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_job_excavators_param,from,to)}
  def countRowsBethInExcavatorsLogTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_excavators_log,from,to)}
  def countRowsBethInParsingErrorsTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_excavators_error_pages,from,to)}
  def countRowsBethInFoundJobsTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_found_jobs,from,to)}
  def countRowsBethInJobTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_jobs,from,to)}
  def countRowsBethInJobsChangesTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_jobs_changes,from,to)}
  def countRowsBethInJobsHiredTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_jobs_hired,from,to)}
  def countRowsBethInJobsApplicantsTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_jobs_applicants,from,to)}
  def countRowsBethInClientChangesTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_clients_changes,from,to)}
  def countRowsBethInClientsWorksHistoryTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_clients_works_history,from,to)}
  def countRowsBethInFoundFreelancersTable(from:Option[Date], to:Option[Date]):Int = {countRowsBeth(odesk_found_freelancers,from,to)}
  def countExcavatorsLogRows(from:Option[Date], to:Option[Date]):Map[String,Map[String,Int]] = { //Return: Map(component -> Map(message type -> count))
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.countExcavatorsLogRows] No created DB.")}
    db.get.withSession(implicit session => {
      //Prepare
      val (wr,p) = buildDateCondition(from,to) match{case Some(p) => (" where ",p); case None => ("","")}
      //Get components list
      val cs = Q.queryNA[String]("select distinct(" + nameColumn + ") from " + odesk_excavators_log + wr + p).list
      //Count for each component
      cs.map(c => {
        val r = logTypesNames.map(t => {
          val q = "select count(*) from " + odesk_excavators_log +
            " where " + nameColumn + " = '" + c + "' and " + typeColumn +" = '" + t + "'" +
            (if(wr != "") " and " else "") + p
          (t, Q.queryNA[Int](q).first)}).toMap
        (c,r)}).toMap})}
  def countFoundByJobsBeth(from:Option[Date], to:Option[Date]):Map[FoundBy,Int] = { //Return: Map(foundByColumn -> count)
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.countFoundByJobsBeth] No created DB.")}
    db.get.withSession(implicit session => {
      //Prepare
      val dp = buildDateCondition(from,to) match{case Some(p) => {" and " + p}; case None => ""}
      //Get components list
      List(FoundBy.Search, FoundBy.Analyse, FoundBy.Unknown).map(fb => {
        val t = Q.queryNA[Int]("select count(*) from " + odesk_found_jobs + " where " + foundByColumn + " = '" + fb.toString + "'" + dp).first
        (fb,t)}).toMap})}
  def countFoundByToScrapPriority(from:Option[Date], to:Option[Date]):Map[Int,Int] = { //Return: Map(priorityColumn -> count)
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.countFoundByToScrapPriority] No created DB.")}
    db.get.withSession(implicit session => {
      //Prepare
      val ((wr,an),dp) = buildDateCondition(from,to) match{case Some(p) => ((" where ", " and "),p); case None => (("",""),"")}
      //Get priorityColumn list
      val ps = Q.queryNA[Int]("select distinct(" + priorityColumn + ") from " + odesk_found_jobs + wr + dp).list
      //Count for each priorityColumn
      ps.map(p => {
        val q = "select count(*) from " + odesk_found_jobs + " where " + priorityColumn + " = " + p  + an + dp
        (p, Q.queryNA[Int](q).first)}).toMap})}
  def countNumberOfAllAndClosedJobsBeth(from:Option[Date], to:Option[Date]):(Int,Int) = { //Return: (total number, number of close)
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.countNumberOfAllAndClosedJobsBeth] No created DB.")}
    db.get.withSession(implicit session => {
      //Prepare
      val ((wr,an),dp) = buildDateCondition(from,to) match{case Some(p) => ((" where ", " and "),p); case None => (("",""),"")}
      //Count
      val tn = Q.queryNA[Int]("select count(*) from " + odesk_jobs + wr + dp).first
      val cn = Q.queryNA[Int]("select count(*) from " + odesk_jobs + " where " + daeDateColumn + " is not null " + an + dp).first
      (tn,cn)})}
  def jobColumnsOccupancy(from:Option[Date], to:Option[Date]):List[(String,Int)] = { //Return: List(column names, number of not null)
    calcOccupancy(odesk_jobs,from,to,jobNullableColumns,jobUnknowableColumns,jobLists)}
  def jobChangesColumnsOccupancy(from:Option[Date], to:Option[Date]):List[(String,Int)] = { //Return: List(column names, number of not null)
    calcOccupancy(odesk_jobs_changes,from,to,jobChangeNullableColumns,jobChangeUnknowableColumns,List())}
  def clientChangesColumnsOccupancy(from:Option[Date], to:Option[Date]):List[(String,Int)] = {
    calcOccupancy(odesk_clients_changes,from,to,clientsChangesNullableColumns,clientsChangesUnknowableColumns,List())}
  def applicantsColumnsOccupancy(from:Option[Date], to:Option[Date]):List[(String,Int)] = {
    calcOccupancy(odesk_jobs_applicants,from,to,applicantsNullableColumns,applicantsUnknowableColumns,List())}
  def hiredColumnsOccupancy(from:Option[Date], to:Option[Date]):List[(String,Int)] = {
    calcOccupancy(odesk_jobs_hired,from,to,hiredNullableColumns,List(),List())}
  def worksHistoryColumnsOccupancy(from:Option[Date], to:Option[Date]):List[(String,Int)] = {
    calcOccupancy(odesk_clients_works_history,from,to,worksHistoryNullableColumns,worksHistoryUnknowableColumns,List())}








}
