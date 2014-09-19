package excavators.odesk
import java.util.Date
import java.awt.Image

/**
 * Contain set of data structures
 * Created by CAB on 18.09.14.
 */



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

trait PaymentMethod
object PaymentMethod{
  case object Unknown extends PaymentMethod
  case object Verified extends PaymentMethod
  case object No extends PaymentMethod}

trait Employment
object Employment{
  case object Unknown extends Employment
  case object AsNeeded extends Employment
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

trait JobState
object JobState{
  case object Unknown extends JobState
  case object InProcess extends JobState
  case object End extends JobState}

//Record from search result
case class FoundWork(
  url:String,
  skills:List[String],
  nFreelancers:Option[Int])

//Search results parsing data
case class ParsedSearchResults(
  works:List[FoundWork],
  nFound:Option[Int],
  nextUrl:Option[String])

//Job page data
case class Job(
  date:Date,
  postDate:Option[Date],
  deadline:Option[Date],
  daeDate:Option[Date],   //Current date if find "Job closed"
  jobTitle:Option[String],
  jobType:Option[String],
  jobPaymentType:Payment,
  jobPrice:Option[Double],
  jobEmployment:Employment,
  jobLength:Option[String],
  jobRequiredLevel:SkillLevel,
  jobDescription:Option[String])

//Job changes data
case class JobChanges(
  date:Date,
  lastViewed:Option[Date],
  nApplicants:Option[Int],
  rateMin:Option[Double],
  rateAvg:Option[Double],
  rateMax:Option[Double],
  interviewing:Option[Int],
  clientDescription:Option[String],
  clientPaymentMethod:PaymentMethod,
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
  date:Date,
  upDate:Option[Date],
  name:Option[String],
  initiatedBy:InitiatedBy,
  url:Option[String])  //Url to freelancer profile if public

//Job hired data
case class JobHired(
  date:Option[Date],
  name:Option[String],
  freelancerId:Option[Long])

//Clients fool work data
case class ClientWork(
  date:Date,
  oUrl:Option[String],
  title:Option[String],
  inProgress:JobState,
  startDate:Option[Date],
  endDate:Option[Date],
  paymentType:Payment,
  billed:Option[Double],
  hours:Option[Int],
  rate:Option[Double],
  freelancerFeedbackText:Option[String],
  freelancerFeedback:Option[Double],
  freelancerName:Option[String],
  freelancerUrl:Option[String],
  clientFeedback:Option[Double])

//Result of job page parsing
case class ParsedJob(
  job:Job,
  changes:JobChanges,
  applicants:List[JobApplicant],
  hires:List[JobHired],
  clientWorks:List[ClientWork])

//Freelancer data
case class Freelancer(
  findDate:Option[Date],
  deleteDate:Option[Date],
  name:Option[String],
  registrationDate:Option[Double])

//Freelancer application data
case class FreelancerApplication(
  date:Option[Date],
  initiatedBy:InitiatedBy,
  title:Option[String],
  jobsId:Option[Long])

//Freelancer changes data
case class FreelancerChanges(
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
  title:Option[String],
  company:Option[String],
  dateFrom:Option[Date],
  dateTo:Option[Date],
  description:Option[String])

//Freelancer education data
case class FreelancerEducation(
  title:Option[String],
  school:Option[String],
  dateFrom:Option[Date],
  dateTo:Option[Date],
  description:Option[String])

//Freelancer other experience data
case class FreelancerOtherExperience(
  title:Option[String],
  date:Option[Date],
  description:Option[String])

//Company data
case class Company(
  findDate:Option[Date],
  deleteDate:Option[Date],
  lastCheckDate:Option[Date],
  name:Option[String],
  title:Option[String],
  logo:Option[Image],
  location:Option[String],
  registrationDate:Option[Date])

//Company changes data
case class CompanyChanges(
  date:Option[Date],
  title:Option[String],
  url:Option[String],
  rate:Option[Double],
  rating:Option[Double],
  allTimeJobs:Option[Int],
  allTimeHours:Option[Int])

//Company work data
case class CompanyWork(
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
  findDate:Option[Date],
  deleteDate:Option[Date],
  workerType:Option[WorkerType],
  freelancerName:Option[String],
  freelancerId:Option[Long],
  rating:Option[Double],
  lastWorkDate:Option[Date])

//Group data
case class Group(
  findDate:Option[Date],
  deleteDate:Option[Date],
  title:Option[String],
  logo:Option[Image])

//Group changes data
case class GroupChanges(
  date:Option[Date],
  nMembers:Option[Int],
  nOpenJobs:Option[Int],
  avgRate:Option[Double],
  avgRating:Option[Double],
  oAvgRate:Option[Double],
  oAvgRating:Option[Double],
  overview:Option[String])











































