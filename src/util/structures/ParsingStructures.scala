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
  isVerified:Verified)

//Freelancer changes data
case class FreelancerChanges(
  createDate:Date,
  name:Option[String],
  link:Option[String],
  title:Option[String],
  profileAccess:Option[String],
  exposeFullName:Option[String],
  role:Option[String],
  emailVerified:Verified,
  videoUrl:Option[String],
  isInviteInterviewAllowed:Allowed,
  availability:FreelancerAvailable,
  availableAgain:Option[String],
  responsivenessScore:Option[String],
  overview:Option[String],
  location:Option[String],
  timeZone:Option[Double],  //Shift from +2
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
  openingTitle:Option[String],
  engagementTitle:Option[String],
  skills:List[String],
  openAccess:Option[String],
  cnyStatus:Option[String],
  financialPrivacy:Option[String],
  isHidden:Hidden,
  agencyName:Option[String],
  segmentationData:Option[String], //JSON Array
  asType:Option[String],
  totalHours:Option[Double],
  rate:Option[Double],
  totalCost:Option[Double],
  chargeRate:Option[Double],
  amount:Option[Double],
  totalHoursPrecise:Option[Double],
  costRate:Option[Double],
  totalCharge:Option[Double],
  ffScores:Map[String,Int],
  ffIsPublic:Option[String],
  ffComment:Option[String],
  ffPrivatePoint:Option[Int],
  ffReasons:List[Option[Int]],
  ffResponse:Option[String],
  ffScore:Option[Double],
  cfScores:Map[String,Int],
  cfIsPublic:Option[String],
  cfComment:Option[String],
  cfResponse:Option[String],
  cfScore:Option[Double],
  lpTitle:Option[String],
  lpThumbnail:Option[String],
  lpIsPublic:Public,
  lpDescription:Option[String],
  lpRecno:Option[String],
  lpCatLevel1:Option[String],
  lpCatRecno:Option[Int],
  lpCatLevel2:Option[String],
  lpCompleted:Option[String],
  lpLargeThumbnail:Option[String],
  lpUrl:Option[String],
  lpProjectContractLinkState:Option[String])

case class FreelancerWorkData(
  createDate:Date,
  rawJson:String,
  jobContractorTier:Option[Int],
  jobSkills:List[String],
  jobUrl:Option[String],   // "/jobs/ + <ciphertext>"
  jobIsPublic:Public,         // originally isPrivate
  jobDescription:Option[String],
  jobCategory:Option[String],
  jobEndDate:Option[Date],
  jobEngagement:Option[String],
  jobDuration:Option[String],
  jobAmount:Option[Double],
  clientTotalFeedback:Option[Int],
  clientScore:Option[Double],
  clientTotalCharge:Option[Double],
  clientTotalHires:Option[Int],
  clientActiveContract:Option[Int],
  clientCountry:Option[String],
  clientCity:Option[String],
  clientTime:Option[String],
  clientMemberSince:Option[Date],
  clientProfileLogo:Option[String],
  clientProfileName:Option[String],
  clientProfileUrl:Option[String],
  clientProfileSummary:Option[String])

case class FreelancerWork(
  record:FreelancerWorkRecord,
  data:FreelancerWorkData)

//Freelancer portfolio data
case class FreelancerPortfolioRecord(
  dataId:Option[String],
  title:Option[String],
  imgUrl:Option[String])

case class FreelancerPortfolioData(
  createDate:Date,
  rawJson:String,
  projectDate:Option[Date],
  title:Option[String],
  description:Option[String],
  imgUrl:Option[String],
  isPublic:Public,
  attachments:List[String],
  creationTs:Option[Date],
  category:Option[String],
  subCategory:Option[String],
  skills:List[String],
  isClient:Client,
  flagComment:Option[String],
  projectUrl:Option[String])

case class FreelancerPortfolio(
  record:FreelancerPortfolioRecord,
  data:FreelancerPortfolioData)

//Freelancer test data
case class FreelancerTestRecord(
  detailsUrl:Option[String],
  title:Option[String],
  score:Option[Double],
  timeComplete:Option[Int])

case class FreelancerTestData(
  createDate:Date,
  oUrl:String,              //Test URL
  title:Option[String],
  scoreSize:Option[Double],
  score:Option[Double],
  time:Option[Int],
  timeComplete:Option[Int],
  rank:Option[Int],
  nTestTake:Option[Int],
  topics:Map[String,Int])

case class FreelancerTest(
  record:FreelancerTestRecord,
  data:FreelancerTestData)

//Freelancer certification data
case class FreelancerCertification(
  rid:Option[String],
  name:Option[String],
  customData:Option[String],
  score:Option[String],
  logoUrl:Option[String],
  certUrl:Option[String],
  isCertVerified:Option[String],
  isVerified:Option[String],
  description:Option[String],
  provider:Option[String],
  skills:List[String],
  dateEarned:Option[String])

//Freelancer employment data
case class FreelancerEmployment(
  recordId:Option[String],
  title:Option[String],
  company:Option[String],
  dateFrom:Option[Date],
  dateTo:Option[Date],
  role:Option[String],
  companyCountry:Option[String],
  companyCity:Option[String],
  description:Option[String])

//Freelancer education data
case class FreelancerEducation(
  school:Option[String],
  areaOfStudy:Option[String],
  degree:Option[String],
  dateFrom:Option[Date],
  dateTo:Option[Date],
  comments:Option[String])

//Freelancer other experience data
case class FreelancerOtherExperience(
  subject:Option[String],
  description:Option[String])

case class FreelancerParsedData(
  rawHtml:String,
  changes:FreelancerChanges,
  works:List[FreelancerWorkRecord],
  portfolio:List[FreelancerPortfolioRecord],
  tests:List[FreelancerTestRecord],
  certification:List[FreelancerCertification],
  employment:List[FreelancerEmployment],
  education:List[FreelancerEducation],
  experience:List[FreelancerOtherExperience])

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











































