package excavators.odesk.db

import java.sql.Timestamp
import java.util.Date
import util.logging.LoggerDBProvider
import util.db.DBProvider
import util.parameters.ParametersMap
import util.structures._
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.StaticQuery

/**
* Provide DB access for oDesk excavators
* Created by CAB on 28.10.14.
*/

class ODeskExcavatorsDBProvider extends DBProvider with LoggerDBProvider {
  //Parameters
  private val maxAccessTry = 20
  private val accessTryTimeout = 500
  //Functions
  private def deserializeExcavatorsParams(s:String):List[(Int, (Boolean, Double))] = {
    if(s != ""){
      s.split(";").map(_.split(",")).toList.flatMap(_.toList match{
        case n :: w :: d :: Nil => List((n.toInt,((w == "W"),d.toDouble)))
        case g => {throw new Exception("Failure on deserialize params: " + s)}})}
    else{
      List()}}
  private def serializeExcavatorsParams(p:List[(Int, (Boolean, Double))]):String = {
    p.map{case (n,(w,d)) => {n + "," + (if(w) "W" else "S") + "," + d}}.mkString(";")}
  private def editExcavatorsParams(f:(List[(Int, (Boolean, Double))])=>(List[(Int, (Boolean, Double))],Int)):Int = { //Return excavator number
    var (en,c) = (-1,0)
    while(en < 0 && c < maxAccessTry){
      db.get.withTransaction(implicit session => {
        //Read parameter
        val p = excavatorsParamTable.filter(_.p_key === excavatorsStatesParamName).first
        if(p._4){ //If params not locked
        //Deserialize and execute
        val ps = deserializeExcavatorsParams(p._3)
          val (np,n)= f(ps)
          en = n
          //Update params
          val snp = serializeExcavatorsParams(np)
          excavatorsParamTable.filter(_.p_key === excavatorsStatesParamName).map(_.p_value).update(snp)
          //Return
          en = n}})
      //Time out if not successful
      if(en < 0){Thread.sleep(accessTryTimeout)}
      c += 1}
    if(en < 0){throw new Exception("[ODeskExcavatorsDBProvider.registrateExcavator] Failed on update param.")}
    en}
  private def getExcavatorsParams(implicit session:Session):List[(Int, (Boolean, Double))] = {
    var ps:Option[List[(Int, (Boolean, Double))]] = None
    var c = 0
    while(ps.isEmpty && c < maxAccessTry){
      //Read parameter
      val p = excavatorsParamTable.filter(_.p_key === excavatorsStatesParamName).first
      if(p._4){ps = Some(deserializeExcavatorsParams(p._3))} //If params not locked deserialize
      c += 1}
    if(ps.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getExcavatorsParams] Failed on get param.")}
    ps.get}
  private def calcJobsDistribution(aes:List[(Int,Double)], nj:Int):List[(Int,Int)] = {
    val t = aes.map{case(e,d) => (e,(nj * d).toInt)}
    val l = t.last match{case(e,d) => (e,(d + nj - t.map(_._2).sum))}
    t.dropRight(1) :+ l}
  //Data methods
  def addParsingErrorRow(d:ParsingErrorRow) = {
   if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.saveLogMessage] No created DB.")}
   db.get.withSession(implicit session => {
     parsingErrorsTable += (
       None,
       new Timestamp(d.createDate.getTime),
       d.oUrl,
       d.msg,
       d.html)})}
  def addJobsRow(d:JobsRow):Option[Long] = { //Return ID of added job row, on None if job with given URL already exist
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addJobsRow] No created DB.")}
    db.get.withSession(implicit session => {
      //Check if job already in foundJobsTableTable or jobTable
      val ni = jobTable.filter(_.o_url === d.foundData.oUrl).firstOption.isEmpty
      //If not save row
      if(ni){jobTable += buildJobsRow(d)}
      //Get ID
      if(ni){
        Some(jobTable.filter(_.o_url === d.foundData.oUrl).map(_.id).first.get)}
      else{
        None}})}
  def addJobsChangesRow(d:JobsChangesRow) = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addJobsChangesRow] No created DB.")}
    db.get.withSession(implicit session => {jobsChangesTable += buildJobsChangesRow(d, d.jobId)})}
  def addClientsChangesRow(d:ClientsChangesRow) = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addClientsChangesRow] No created DB.")}
    db.get.withSession(implicit session => {clientsChangesTable += buildClientsChangesRow(d, d.jobId)})}
  def addJobsApplicantsRow(d:JobsApplicantsRow) = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addJobsApplicantsRow] No created DB.")}
    db.get.withSession(implicit session => {jobsApplicantsTable += buildJobsApplicantsRows(d, d.jobId)})}
  def addJobsHiredRow(d:JobsHiredRow) = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addJobsHiredRow] No created DB.")}
    db.get.withSession(implicit session => {jobsHiredTable += buildJobsHiredRows(d, d.jobId)})}
  def addClientsWorksHistoryRow(d:ClientsWorksHistoryRow):Boolean = { // Return true if row been insert, and false if row with given URL already exist
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addClientsWorksHistoryRow] No created DB.")}
    db.get.withSession(implicit session => {
      //Check if job already exist
      val ni = d.workData.oUrl match{
        case Some(url) => clientsWorksHistoryTable.filter(r => r.o_url.isDefined && r.o_url === url).firstOption.isEmpty
        case None => true}  //Add anyway if no URL
      //If not then insert
      if(ni){clientsWorksHistoryTable += buildClientsWorksHistoryRow(d, d.jobId)}
    ni})}
  def addFoundFreelancerRow(d:FoundFreelancerRow):Boolean = {    // Return true if row been insert, and false if row with given URL already exist
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addFoundFreelancerRow] No created DB.")}
    db.get.withSession(implicit session => {
      //Check if job already exist
      val ni = foundFreelancersTable.filter(_.o_url === d.oUrl).firstOption.isEmpty
      //If not then insert
      if(ni){foundFreelancersTable += buildFoundFreelancerRow(d)}
      ni})}
  def getSetOfLastJobsURLoFundBy(size:Int, fb:FoundBy):Set[String] = {
    if(db.isEmpty || databaseName.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getSetOfLastJobsURL] No created DB.")}
    val r = db.get.withSession(implicit session => {
      //Get auto inctement count and calc min id
      val cq = StaticQuery.query[String, Int]("SELECT `AUTO_INCREMENT` FROM  INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + databaseName.get + "' AND TABLE_NAME = ? ;")
      def nln(n:Int):Int = {val t = n - size; if(t < 0){0}else{t}}
      val mif = nln(cq("odesk_found_jobs").first)
      val mij = nln(cq("odesk_jobs").first)
      //Get last oURLs
      val dqf = foundJobsTable.filter(_.found_by ===  fb.toString).filter(_.id >= mif.toLong).map(_.o_url).list
      val dqj = jobTable.filter(_.found_by === fb.toString).filter(_.id >= mij.toLong ).map(_.o_url).list
      //Return set
      dqf.toSet ++ dqj.toSet})
    r}
  def getNOfFoundByJobs(n:Int, fb:FoundBy):(List[FoundJobsRow], Int) = {  //Return list of N rows with max priorityColumn, and total rows in table
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getNOfFoundByJobs] No created DB.")}
    db.get.withSession(implicit session => {
      //Gen older rows
      val nr = foundJobsTable.length.run
      val rs = foundJobsTable.filter(_.found_by === fb.toString).sortBy(_.create_date).take(n).list.map{
        case(id:Option[Long], url:String, fb:String, d:Timestamp, p:Int, sks:String, nf:Option[Int]) => {
          buildFoundJobsRows(id, url, fb, d, p, sks, nf)}}
      //Return result
      (rs,nr)})}
  def getNOfFoundByExcavatorNumber(n:Int, en:Int):(List[FoundJobsRow], Int) = {  //Return list of N rows for given excavator and tonal number of rows
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getNOfFoundByExcavatorNumber] No created DB.")}
    db.get.withSession(implicit session => {
      //Gen table size
      val nr = foundJobsTable.length.run
      //Gen older rows
      val rs = foundJobsTable.filter(_.priority === en).sortBy(_.create_date).take(n).list.map{
        case(id:Option[Long], url:String, fb:String, d:Timestamp, p:Int, sks:String, nf:Option[Int]) => {
          buildFoundJobsRows(id, url, fb, d, p, sks, nf)}}
      //Return result
      (rs,nr)})}
  def getFreelancerIdByURL(url:String):Option[Long] = { //Return row ID by freelancer page URL
    //!!! Non implemented
    None}
  def isJobScraped(url:String):Option[(Long,JobAvailable)] = { //Return ID if url in odesk_jobs
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.isJobScraped] No created DB.")}
    db.get.withSession(implicit session => {
      jobTable.filter(_.o_url === url).list match{
        case e :: _ => e._1.flatMap(id => {
          jobsChangesTable.filter(_.id === id).list match{
            case s :: _ => Some((id, JobAvailable.formString(s._4)))
            case _ => None}})
        case _ => None}})}
  def setNextJobCheckTime(id:Long, d:Option[Date]) = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.setNextJobCheckTime] No created DB.")}
    db.get.withSession(implicit session => {
      val q = jobTable.filter(_.id === id).map(_.next_check_date)
      q.update(d.map(t => new Timestamp(t.getTime)))})}
  def delFoundJobRow(id:Long) = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.delFoundJobRow] No created DB.")}
    db.get.withSession(implicit session => {
      val q = foundJobsTable.filter(_.id === id)
      q.delete})}
  def loadParameters():ParametersMap = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.loadParameters] No created DB.")}
    db.get.withSession(implicit session => {
      //Get params
      val prs = excavatorsParamTable.filter(_.is_active === true).map(r => (r.p_key, r.p_value)).list
      //Check if no two active
      val ks = prs.map(_._1)
      if(ks.size != ks.toSet.size){
        throw new Exception("[ODeskExcavatorsDBProvider.loadParameters] Several ative params with same name: " + prs)}
      //Reset update flag
      if(ks.contains(need_update_param_name)){
        val q = excavatorsParamTable.filter(_.p_key === need_update_param_name).map(_.p_value)
        q.update(Update.Updated.toString)}
      ParametersMap(prs.toMap)})}
  def checkParametersUpdateFlag():Boolean = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.checkParametersUpdateFlag] No created DB.")}
    db.get.withSession(implicit session => {
      excavatorsParamTable.filter(_.p_key === need_update_param_name).map(_.p_value).firstOption match {
        case Some(v) => Update.formString(v) == Update.NeedUpdate
        case None => false}})}
  def addAllJobDataAndDelFromFound(d:AllJobData):(Int,Int,Int,Int,Int, Long) = { //If added return N added: applicants,hired,clients works,found freelancer,found jobs, Job ID
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.addAllJobDataAndDelFromFound] No created DB.")}
    db.get.withTransaction(implicit session => {
      //Get existence jobs and params
      val jus = (d.foundJobsRows.map(_.oUrl) ++ d.clientsWorksHistoryRows.flatMap(_.workData.oUrl)).toSet
      val ejs = (jobTable.filter(_.o_url inSetBind jus).map(_.o_url) ++ foundJobsTable.filter(_.o_url inSetBind jus).map(_.o_url)).list.toSet
      val ps = getExcavatorsParams
      //Get existence freelancers(if non empty)
      val fus = d.foundFreelancerRows.map(_.oUrl).toSet
      val efs = foundFreelancersTable.filter(_.o_url inSetBind fus).map(_.o_url).list.toSet
      //Insert job
      jobTable += buildJobsRow(d.jobsRow)
      val id = StaticQuery.queryNA[Long]("SELECT LAST_INSERT_ID();").first
      //Isert additional data
      jobsChangesTable += buildJobsChangesRow(d.jobsChangesRow, id)
      clientsChangesTable += buildClientsChangesRow(d.clientsChangesRow, id)
      //Insert applicants and hired
      jobsApplicantsTable ++= d.jobsApplicantsRows.map(r => buildJobsApplicantsRows(r, id))
      jobsHiredTable ++= d.jobsHiredRows.map(r => buildJobsHiredRows(r, id))
      //Insert clients works history
      val wrs = d.clientsWorksHistoryRows.filter(r => {r.workData.oUrl.isEmpty || (! ejs.contains(r.workData.oUrl.get))})
      clientsWorksHistoryTable ++= wrs.map(r => buildClientsWorksHistoryRow(r, id))
      //Insert found jobs
      val ajs = d.foundJobsRows.filter(r => {! ejs.contains(r.oUrl)})
      if(ajs.nonEmpty){
        val aes = ps.filter{case(_,(w,d)) => {w && d != 0}}.map{case(e,(_,d)) => (e,d)} //Active excavators distribution
        if(aes.nonEmpty){
          //Calc distribution
          val ds = calcJobsDistribution(aes, ajs.size)
          //Insert
          def rds(ds:List[(Int,Int)], js:List[FoundJobsRow]):List[FoundJobsRowType] = ds match{
            case (e,ds) :: l => {
              val uis = js.take(ds)
             rds(l, js.drop(ds)) ++ uis.map(r => buildFoundJobsRow(r,e))}
            case Nil => List()}
          foundJobsTable ++= rds(ds,ajs)}
        else{
          foundJobsTable ++= ajs.map(r => buildFoundJobsRow(r,0))}}  //All new job to defoult excavator
      //Insert found freelancer
      val frs = d.foundFreelancerRows.filter(r => {! efs.contains(r.oUrl)})
      foundFreelancersTable ++= frs.map(r => buildFoundFreelancerRow(r))
      //Delete job from found
      foundJobsTable.filter(_.o_url === d.jobsRow.foundData.oUrl).delete
      //Calc and return result
      (d.jobsApplicantsRows.size, d.jobsHiredRows.size, wrs.size, frs.size, ajs.size, id)})}
  def countFoundByToScrapPriority:Map[Int,Int] = { //Return: Map(priorityColumn -> count)
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.countFoundByToScrapPriority] No created DB.")}
    db.get.withSession(implicit session => {
      //Get priorityColumn list
      val ps = StaticQuery.queryNA[Int]("select distinct(" + priorityColumn + ") from " + odesk_found_jobs).list
      //Count for each priorityColumn
      ps.map(p => {
        val q = "select count(*) from " + odesk_found_jobs + " where " + priorityColumn + " = " + p
        (p, StaticQuery.queryNA[Int](q).first)}).toMap})}
  def getExcavatorsStateParam(setLock:Boolean):(Map[Int,(Boolean,Double)],Boolean) = { //Return: (Map(excavator number -> (is excavator work, distribution priority)), prev lock state)
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.getExcavatorsStateParam] No created DB.")}
    db.get.withTransaction(implicit session => {
      //Read parameter
      val p = excavatorsParamTable.filter(_.p_key === excavatorsStatesParamName).first
      //Deserialize params
      val pm = deserializeExcavatorsParams(p._3)
      //Set locked if need
      if(setLock){
        excavatorsParamTable.filter(_.p_key === excavatorsStatesParamName).map(_.is_active).update(false)}
      //Return data
      (pm.toMap,(! p._4))})}
  def updateExcavatorsStateParam(param:Map[Int,(Boolean,Double)],setLock:Boolean)= {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.updateExcavatorsStateParam] No created DB.")}
    db.get.withTransaction(implicit session => {
      //Serialize params
      val p = serializeExcavatorsParams(param.toList)
      //Update
      excavatorsParamTable.filter(_.p_key === excavatorsStatesParamName).map(_.p_value).update(p)
      //Set locked/unlocked
      excavatorsParamTable.filter(_.p_key === excavatorsStatesParamName).map(_.is_active).update(! setLock)})}
  def redistributeFoundJobsFromToWithProb(from:List[Int], to:List[(Int,Double)])= {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.redistributeFoundJobsFromToWithProb] No created DB.")}
    db.get.withTransaction(implicit session => {
      //Get IDs of row to redistribute
      val ojs = foundJobsTable.filter(_.priority inSetBind from).map(_.id).list.flatMap(e => e)
      //Calc distribution
      val ds = calcJobsDistribution(to,ojs.size)
      //Redistribute
      def rds(ds:List[(Int,Int)], ids:List[Long]):Unit = ds match{
        case (e,d) :: l => {
          val uis = ids.take(d)
          foundJobsTable.filter(_.id inSetBind uis).map(_.priority).update(e)
          rds(l, ids.drop(d))}
        case Nil =>}
      rds(ds,ojs)})}
  def registrateExcavator:Int = { //Return number of excavator
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.registrateExcavator] No created DB.")}
    editExcavatorsParams(ps => {
      val ex = ps.map(_._1)
      val n = (1 to 100).find(i => {! ex.contains(i)}).get
      ((ps :+ (n,(true,0.0))),n)})}
  def unregistrateExcavator(en:Int) = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.unregistrateExcavator] No created DB.")}
    editExcavatorsParams(ps => {
      (ps.map{case(e,(w,d)) => if(e == en){(e,(false,0.0))}else{(e,(w,d))}},0)})}
  def addAllFreelancerDataAndDelFromFound(d:AllFreelancerData) = {











  }



}













































