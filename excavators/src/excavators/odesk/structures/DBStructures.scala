package excavators.odesk.structures
import java.sql.Date

/**
 * Structures using in for DB interaction
 * Created by CAB on 22.09.14.
 */

case class FoundJobsRow(
  id:Long,
  oUrl:String,
  foundBy:FoundBy,
  date:Date,
  priority:Int,
  skills:List[String],
  nFreelancers:Option[Int])

case class JobsRow(
  id:Long,
  foundData:FoundJobsRow,
  daeDate:Option[Date],
  deleteDate:Option[Date],
  nextCheckDate:Option[Date],
  jabData:Job)

case class JobsChangesRow(
  id:Long,
  jobId:Long,
  changeData:JobChanges)

case class JobsApplicantsRow(
  id:Long,
  jobId:Long,
  applicantData:JobApplicant,
  freelancerId:Option[Long])

case class JobsHiredRow(
  id:Long,
  jobId:Long,
  hiredData:JobHired,
  freelancerId:Option[Long])

case class ClientsWorksHistoryRow(
  id:Long,
  jobId:Long,
  workData:ClientWork,
  freelancerId:Option[Long])

