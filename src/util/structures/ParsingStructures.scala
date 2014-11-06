package util.structures

import java.awt.Image
import java.util.Date

/**
 * Contain set of data structures for parsing results
 * Created by CAB on 18.09.14.
 */

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
  createDate:Date,
  postDate:Option[Date],
  deadline:Option[Date],
  jobTitle:Option[String],
  jobType:Option[String],
  jobPaymentType:Payment,
  jobPrice:Option[Double],
  jobEmployment:Employment,
  jobLength:Option[String],
  jobRequiredLevel:SkillLevel,
  jobQualifications:Map[String,String],
  jobDescription:Option[String])

//Job changes data
case class JobChanges(
  createDate:Date,
  jobAvailable:JobAvailable, //No if found "This job is no longer available "
  lastViewed:Option[Date],
  nApplicants:Option[Int],
  applicantsAvg:Option[Double],
  rateMin:Option[Double],
  rateAvg:Option[Double],
  rateMax:Option[Double],
  nInterviewing:Option[Int],
  interviewingAvg:Option[Double],
  nHires:Option[Int])

//Client changes data
case class ClientChanges(
  createDate:Date,
  name:Option[String],
  logoUrl:Option[String],
  url:Option[String],
  description:Option[String],
  paymentMethod:PaymentMethod,
  rating:Option[Double],
  nReviews:Option[Int],
  location:Option[String],
  time:Option[String],
  nJobs:Option[Int],
  hireRate:Option[Int],
  nOpenJobs:Option[Int],
  totalSpend:Option[Double],
  nHires:Option[Int],
  nActive:Option[Int],
  avgRate:Option[Double],
  hours:Option[Int],
  registrationDate:Option[Date])

//Job applicant data
case class JobApplicant(
  createDate:Date,
  upDate:Option[Date],
  name:Option[String],
  initiatedBy:InitiatedBy,
  url:Option[String])  //Url to freelancer profile if public

//Job hired data
case class JobHired(
  createDate:Date,
  name:Option[String],
  freelancerUrl:Option[String])

//Clients fool work data
case class ClientWork(
  createDate:Date,
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
  jobChanges:JobChanges,
  clientChanges:ClientChanges,
  applicants:List[JobApplicant],
  hires:List[JobHired],
  clientWorks:List[ClientWork])

////Freelancer data
//case class Freelancer(
//  createDate:Date,
//  name:Option[String],
//  registrationDate:Option[Double])

////Freelancer application data
//case class FreelancerApplication(
//  createDate:Date,
//  initiatedBy:InitiatedBy,
//  title:Option[String],
//  jobsId:Option[Long])

case class FreelancerLanguage(
  name:String,
  level:Option[Int],
  isVerified:Option[Boolean])

//Freelancer changes data
case class FreelancerChanges(
  createDate:Date,
  name:Option[String],
  link:Option[String],
  title:Option[String],
  profileAccess:Option[String],
  exposeFullName:Option[String],
  role:Option[String],
  emailVerified:Option[Boolean],
  videoUrl:Option[String],
  isInviteInterviewAllowed:Option[Boolean],
  availability:FreelancerAvailable,
  availableAgain:Option[String],
  responsivenessScore:Option[String],
  overview:Option[String],
  location:Option[String],
  timeZone:Option[Int],  //Shift from +2
  languages:List[FreelancerLanguage],
  photoUrl:Option[String],
  rate:Option[Double],
  rentPercent:Option[Int],
  rating:Option[Double],
  allTimeJobs:Option[Int],
  allTimeHours:Option[Int],
  skills:List[String],
  companyUrl:Option[String],
  companyLogoUrl:Option[String])

//Freelancer work data
case class FreelancerWorkRecord(
  jobKey:Option[String],
  paymentType:Payment,
  status:Status,
  startDate:Option[Date],
  endDate:Option[Date],
  fromFull:Option[Date],
  toFull:Option[Date],
  openTitle:Option[String],
  endTitle:Option[String],
  skills:List[String],
  openAccess:Access,
  cnyStatus:Option[String],
  financialPrivacy:Option[String],
  isHidden:Option[Double],
  agencyName:Option[String],
  segmentationData:Option[String], //JSON Array
  asType:Option[String],
  totalHours:Option[Double],
  rate:Option[Double],
  totalCost:Option[Double],
  chargeRate:Option[Double],
  amount:Option[Double],
  totalHoursPrecise:Option[Double],
  blendedRate:Option[Double],
  totalCharge:Option[Double],
  ffScores:Map[String,Int],
  ffIsPublic:Option[String],
  ffComment:Option[String],
  ffPrivatePoint:Option[Int],
  ffReasons:List[Int],
  ffResponse:Option[String],
  ffScore:Option[Double],
  cfScores:Map[String,Int],
  cfIsPublic:Option[Int],
  cfComment:Option[String],
  cfResponse:Option[String],
  cfScore:Option[Double])

case class FreelancerWorkData(
  JobContractorTier:Option[Int],
  JobSkills:List[String],
  JobJobUrl:Option[String], // "/jobs/ + <ciphertext>"
  JobIsPrivate:Option[Boolean],
  JobDescription:Option[String],
  JobCategory:Option[String],
  JobEndDate:Option[Date],
  JobEngagement:Option[String],
  JobDuration:Option[String],
  JobAmount:Option[Double],
  clientFeedback:Option[Double],
  clientScore:Option[Double],
  clientTotalCharge:Option[Double],
  clientTotalHires:Option[Int],
  clientActiveContract:Option[Int],
  clientCountry:Option[String],
  clientCity:Option[String],
  clientTime:Option[Int],      //Shift from +2
  clientMemberSince:Option[Date])

case class FreelancerWork(
  record:FreelancerWorkRecord,
  data:FreelancerWorkData)

//Freelancer portfolio data
case class FreelancerPortfolio(
  oUrl:String,
  createDate:Option[Date],
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
  createDate:Option[Date],
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
  createDate:Option[Date],
  description:Option[String])

case class FreelancerParsedData(
  changes:FreelancerChanges,
  works:List[FreelancerWorkRecord]

                                 )


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
  createDate:Option[Date],
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
  createDate:Option[Date],
  nMembers:Option[Int],
  nOpenJobs:Option[Int],
  avgRate:Option[Double],
  avgRating:Option[Double],
  oAvgRate:Option[Double],
  oAvgRating:Option[Double],
  overview:Option[String])











































