package util.structures

import java.awt.image.BufferedImage
import java.util.Date

/**
 * Structures using in for DB interaction
 * Created by CAB on 22.09.14.
 */

//Service

case class ParsingErrorRow(
  id:Long,
  createDate:Date,
  oUrl:String,
  msg:String,
  html:String)

//Jobs

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

case class ClientsChangesRow(
  id:Long,
  jobId:Long,
  changeData:ClientChanges,
  logo:Option[BufferedImage])

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

case class ToTrackingJob(
  id:Long,
  oUrl:String,
  date:Date,
  priority:Int)

case class AllJobData(
  jobsRow:JobsRow,
  jobsChangesRow:JobsChangesRow,
  clientsChangesRow:ClientsChangesRow,
  jobsApplicantsRows:List[JobsApplicantsRow],
  jobsHiredRows:List[JobsHiredRow],
  clientsWorksHistoryRows:List[ClientsWorksHistoryRow],
  foundFreelancerRows:List[FoundFreelancerRow],
  foundJobsRows:List[FoundJobsRow])

//Freelances Согласен,

case class FreelancerRowHeader(
  id:Long,
  freelancerId:Long,
  createDate:Date)

case class FreelancerWorkRowHeader(
  id:Long,
  freelancerId:Long,
  workId:Long,
  createDate:Date)

case class FoundFreelancerRow(
  id:Long,
  oUrl:String,
  key:String,     //Last part of URL
  date:Date,
  priority:Int)

case class FreelancerRow(
  id:Long,
  createDate:Date,
  foundDate:Date,
  oUrl:String,
  key:String)

case class FreelancerRawHtmlRow(
  header:FreelancerRowHeader,
  html:String)

case class FreelancerRawJobJsonRow(
  header:FreelancerRowHeader,
  json:String)

case class FreelancerRawPortfolioJsonRow(
  header:FreelancerRowHeader,
  json:String)

case class FreelancerMainChangeRow(
  header:FreelancerRowHeader,
  name:Option[String],
  profileAccess:Option[String],
  link:Option[String],
  exposeFullName:Option[String],
  role:Option[String],
  videoUrl:Option[String],
  isInviteInterviewAllowed:Allowed,
  location:Option[String],
  timeZone:Option[Double],  //Shift from +2
  emailVerified:Verified,
  photo:Option[BufferedImage],
  companyUrl:Option[String],
  companyLogo:Option[BufferedImage])

case class FreelancerAdditionalChangeRow(
  header:FreelancerRowHeader,
  title:Option[String],
  availability:FreelancerAvailable,
  availableAgain:Option[String],
  responsivenessScore:Option[String],
  overview:Option[String],
  languages:List[FreelancerLanguage],
  rate:Option[Double],
  rentPercent:Option[Int],
  rating:Option[Double],
  allTimeJobs:Option[Int],
  allTimeHours:Option[Int],
  skills:List[String])

case class FreelancerWorkRow(
  header:FreelancerRowHeader,
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
  segmentationData:Option[String]) //JSON Array

case class FreelancerWorkAdditionalDataRow(
  header:FreelancerWorkRowHeader,
  asType:Option[String],
  totalHours:Option[Double],
  rate:Option[Double],
  totalCost:Option[Double],
  chargeRate:Option[Double],
  amount:Option[Double],
  totalHoursPrecise:Option[Double],
  costRate:Option[Double],
  totalCharge:Option[Double],
  jobContractorTier:Option[Int],
  jobUrl:Option[String],
  jobDescription:Option[String],
  jobCategory:Option[String],
  jobEngagement:Option[String],
  jobDuration:Option[String],
  jobAmount:Option[Double])

case class FreelancerWorkFeedbackRow(
  header:FreelancerWorkRowHeader,
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
  cfScore:Option[Double])

case class FreelancerLinkedProjectDataRow(
  header:FreelancerWorkRowHeader,
  lpTitle:Option[String],
  lpThumbnail:Option[String],
  lpIsPublic:Public,
  lpDescription:Option[String],
  lpRecno:Option[String],
  lpCatLevel1:Option[String],
  lpCatRecno:Option[String],
  lpCatLevel2:Option[String],
  lpCompleted:Option[String],
  lpLargeThumbnail:Option[String],
  lpUrl:Option[String],
  lpProjectContractLinkState:Option[String])

case class FreelancerWorkClientRow(
  header:FreelancerWorkRowHeader,
  clientTotalFeedback:Option[Int],
  clientScore:Option[Double],
  clientTotalCharge:Option[Double],
  clientTotalHires:Option[Int],
  clientActiveContract:Option[Int],
  clientCountry:Option[String],
  clientCity:Option[String],
  clientTime:Option[String],
  clientMemberSince:Option[Date],
  clientProfileLogo:Option[BufferedImage],
  clientProfileName:Option[String],
  clientProfileUrl:Option[String],
  clientProfileSummary:Option[String])

case class FreelancerPortfolioRow(
  header:FreelancerRowHeader,
  projectDate:Option[Date],
  title:Option[String],
  description:Option[String],
  isPublic:Public,
  attachments:List[String],
  creationTs:Option[Date],
  category:Option[String],
  subCategory:Option[String],
  skills:List[String],
  isClient:Client,
  flagComment:Option[String],
  projectUrl:Option[String],
  imgUrl:Option[String])

case class FreelancerTestRow(
  header:FreelancerRowHeader,
  data:FreelancerTestRecord)

case class FreelancerCertificationRow(
  header:FreelancerRowHeader,
  data:FreelancerCertification)

case class FreelancerEmploymentRow(
  header:FreelancerRowHeader,
  data:FreelancerEmployment)

case class FreelancerEducationRow(
  header:FreelancerRowHeader,
  data:FreelancerEducation)

case class FreelancerOtherExperienceRow(
  header:FreelancerRowHeader,
  data:FreelancerOtherExperience)

case class FreelancerWorkDataRow(
  workRow:FreelancerWorkRow,
  workAdditionalDataRow:FreelancerWorkAdditionalDataRow,
  workFeedbackRow:FreelancerWorkFeedbackRow,
  linkedProjectDataRow:FreelancerLinkedProjectDataRow,
  workClientRow:FreelancerWorkClientRow)

case class AllFreelancerData(
  freelancerRow:FreelancerRow,
  rawHtmlRow:FreelancerRawHtmlRow,
  rawJobJsonRow:List[FreelancerRawJobJsonRow],
  rawPortfolioJsonRow:List[FreelancerRawPortfolioJsonRow],
  mainChangeRow:FreelancerMainChangeRow,
  additionalChangeRow:FreelancerAdditionalChangeRow,
  works:List[FreelancerWorkDataRow],
  portfolioRows:List[FreelancerPortfolioRow],
  testRows:List[FreelancerTestRow],
  certificationRows:List[FreelancerCertificationRow],
  employmentRows:List[FreelancerEmploymentRow],
  educationRows:List[FreelancerEducationRow],
  otherExperienceRows:List[FreelancerOtherExperienceRow],
  foundJobsRows:List[FoundJobsRow])




















































