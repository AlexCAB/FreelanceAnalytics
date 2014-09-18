package excavators.odesk
import java.util.Date
import java.awt.Image


trait FoundBy
object FoundBy{
   case object Unknown extends FoundBy
   case object Search extends FoundBy
   case object Analyse extends FoundBy}

trait Payment
object Payment{
  case object Unknown extends Payment
  case object Hourly extends Payment
  case object Budget extends Payment}

trait Employment
object Employment{
  case object Unknown extends Employment
  case object Full extends Employment
  case object Part extends Employment}

trait InitiatedBy
object InitiatedBy{
  case object Unknown extends InitiatedBy
  case object Freelancer extends InitiatedBy
  case object Client extends InitiatedBy}

trait SkillLevel
object SkillLevel{
  case object Unknown extends SkillLevel
  case object Entry extends SkillLevel
  case object Intermediate extends SkillLevel
  case object Expert extends SkillLevel}

trait WorkerType
object WorkerType{
  case object Unknown extends WorkerType
  case object Freelancer extends WorkerType
  case object Manager extends WorkerType}

//Record from search result
case class FoundWork(
  url:String,
  skills:List[String],
  nFreelancers:Option[Int])

//Job page data
case class Job(
  id:Long,
  oUrl:String,
  foundBy:FoundBy,
  postDate:Option[Date],
  deadline:Option[Date],
  daeDate:Option[Date],
  deleteDate:Option[Date],
  nextCheckDate:Option[Date],
  jobTitle:Option[String],
  jobTypeTags:List[String],
  jobPaymentType:Payment,
  jobPrice:Option[Double],
  jobEmployment:Employment,
  jobLength:Option[Int],
  jobRequiredLevel:SkillLevel,
  jobSkills:List[String],
  jobDescription:Option[String],
  changes:List[JobChanges],
  applicans:List[JobApplicant],
  hireds:List[JobHired],
  works:List[ClientWork])

//Job changes data
case class JobChanges(
  id:Long,
  jobId:Long,
  date:Option[Date],
  lastViewed:Option[Date],
  nApplicants:Option[Double],
  rateMin:Option[Double],
  rateAvg:Option[Double],
  rateMax:Option[Double],
  interviewing:Option[Int],
  clientDescription:Option[String],
  clientRating:Option[Double],
  clientNReviews:Option[Int],
  clientLocation:Option[String],
  clientNJobs:Option[Int],
  clientHireRate:Option[Int],
  clientNOpenJobs:Option[Int],
  clientTotalSpend:Option[Double],
  clientNHires:Option[Int],
  clientNActive:Option[Int],
  clientAvgRate:Option[Double],
  clientHours:Option[Int],
  clientRegistrationDate:Option[Date])

//Job applicant data
case class JobApplicant(
  id:Long,
  jobId:Long,
  date:Option[Date],
  name:Option[String],
  initiatedBy:InitiatedBy,
  freelancerId:Option[Long])

//Job hired data
case class JobHired(
  id:Long,
  jobId:Long,
  date:Option[Date],
  name:Option[String],
  freelancerId:Option[Long])

//Clients work data
case class ClientWork(
  id:Long,
  jobId:Long,
  jobsTableId:Option[Long],
  oUrl:Option[String],
  title:Option[String],
  startDate:Option[Date],
  endDate:Option[Date],
  paymentType:Payment,
  billed:Option[Double],
  hours:Option[Int],
  rate:Option[Double],
  freelancerFeedbackText:Option[String],
  freelancerFeedback:Option[Double],
  freelancerName:Option[String],
  freelancerId:Option[Long],
  clientFeedback:Option[Double])

//Freelancer data
case class Freelancer(
  id:Long,
  oUrl:String,
  findDate:Option[Date],
  deleteDate:Option[Date],
  nextCheckDate:Option[Date],
  name:Option[String],
  registrationDate:Option[Double],
  applications:List[FreelancerApplication],
  changes:List[FreelancerChanges],
  works:List[FreelancerWork],
  portfolios:List[FreelancerPortfolio],
  tests:List[FreelancerTest],
  employments:List[FreelancerEmployment],
  educations:List[FreelancerEducation],
  experiences:List[FreelancerOtherExperience])

//Freelancer application data
case class FreelancerApplication(
  id:Long,
  freelancerId:Long,
  date:Option[Date],
  initiatedBy:InitiatedBy,
  title:Option[String],
  jobsId:Option[Long])

//Freelancer changes data
case class FreelancerChanges(
  id:Long,
  freelancerId:Long,
  date:Option[Date],
  location:Option[String],
  timeZone:Option[Int],
  languages:List[String],
  lastWorkDate:Option[Date],
  photo:Option[Image],
  title:Option[String],
  rate:Option[Double],
  rating:Option[Double],
  allTimeJobs:Option[Int],
  allTimeHours:Option[Int],
  skills:List[String],
  overview:Option[String],
  companyId:Option[Long])

//Freelancer work data
case class FreelancerWork(
  id:Long,
  freelancerId:Long,
  title:Option[String],
  jobId:Option[Long],
  startDate:Option[Date],
  endDate:Option[Date],
  paymentType:Payment,
  billed:Option[Double],
  hours:Option[Int],
  rate:Option[Double],
  description:Option[String],
  skillsRating:List[String],
  qualityRating:Option[Double],
  availabilityRating:Option[Double],
  deadlinesRating:Option[Double],
  communicationRating:Option[Double],
  cooperationRating:Option[Double],
  feedbackText:Option[String],
  freelancerFeedbackRating:Option[Double],
  freelancerFeedbackTxt:Option[String])

//Freelancer portfolio data
case class FreelancerPortfolio(
  id:Long,
  freelancerId:Long,
  oUrl:String,
  date:Option[Date],
  title:Option[String],
  description:Option[String],
  img:Option[Image],
  category:Option[String],
  subCategory:Option[String],
  skills:List[String],
  url:Option[String])

//Freelancer test data
case class FreelancerTest(
  id:Long,
  freelancerId:Long,
  oUrl:String,
  title:Option[String],
  date:Option[Date],
  scoreSize:Option[Double],
  score:Option[Double],
  time:Option[Int],
  timeComplete:Option[Int],
  rank:Option[Int],
  nTestTake:Option[Int],
  topics:Map[String,Int])

//Freelancer employment data
case class FreelancerEmployment(
  id:Long,
  freelancerId:Long,
  title:Option[String],
  company:Option[String],
  dateFrom:Option[Date],
  dateTo:Option[Date],
  description:Option[String])

//Freelancer education data
case class FreelancerEducation(
  id:Long,
  freelancerId:Long,
  title:Option[String],
  school:Option[String],
  dateFrom:Option[Date],
  dateTo:Option[Date],
  description:Option[String])

//Freelancer other experience data
case class FreelancerOtherExperience(
  id:Long,
  freelancerId:Long,
  title:Option[String],
  date:Option[Date],
  description:Option[String])

//Company data
case class Company(
  id:Long,
  oUrl:String,
  findDate:Option[Date],
  deleteDate:Option[Date],
  lastCheckDate:Option[Date],
  name:Option[String],
  title:Option[String],
  logo:Option[Image],
  location:Option[String],
  registrationDate:Option[Date],
  changes:List[CompanyChanges],
  works:List[CompanyWork],
  workers:List[CompanyWorker])

//Company changes data
case class CompanyChanges(
  id:Long,
  companyId:Long,
  date:Option[Date],
  nextCheckDate:Option[Date],
  title:Option[String],
  url:Option[String],
  rate:Option[Double],
  rating:Option[Double],
  allTimeJobs:Option[Int],
  allTimeHours:Option[Int])

//Company work data
case class CompanyWork(
  id:Long,
  company_id:Long,
  title:Option[String],
  jobId:Option[Long],
  freelancerName:Option[String],
  freelancerId:Option[Long],
  startDate:Option[Date],
  endDate:Option[Date],
  paymentType:Payment,
  billed:Option[Double],
  hours:Option[Int],
  rate:Option[Double],
  description:Option[String],
  skillsRating:Option[Double],
  qualityRating:Option[Double],
  availabilityRating:Option[Double],
  deadlinesRating:Option[Double],
  communicationRating:Option[Double],
  cooperationRating:Option[Double],
  feedbackText:Option[String],
  freelancerFeedbackRating:Option[Double],
  freelancerFeedbackTxt:Option[String])

//Company worker data
case class CompanyWorker(
  id:Long,
  companyId:Long,
  findDate:Option[Date],
  deleteDate:Option[Date],
  workerType:Option[WorkerType],
  freelancerName:Option[String],
  freelancerId:Option[Long],
  rating:Option[Double],
  lastWorkDate:Option[Date])

//Group data
case class Group(
  id:Long,
  oUrl:String,
  findDate:Option[Date],
  deleteDate:Option[Date],
  nextCheckDate:Option[Date],
  title:Option[String],
  logo:Option[Image],
  changes:List[GroupChanges])

//Group changes data
case class GroupChanges(
  id:Long,
  groupId:Long,
  date:Option[Date],
  nMembers:Option[Int],
  nOpenJobs:Option[Int],
  avgRate:Option[Double],
  avgRating:Option[Double],
  oAvgRate:Option[Double],
  oAvgRating:Option[Double],
  overview:Option[String])











































