package excavators.odesk.db
import org.scalatest._
import java.sql.Date
import excavators.odesk.structures._
import java.awt.image.BufferedImage

/**
 * Test for DBProvider class
 * (!)Test only on empty DB
 * Created by CAB on 22.09.14.
 */

class DBProviderTest extends WordSpecLike with Matchers {
  //Helpers
  val foundJobsRow1 = FoundJobsRow(
    id = 1001L,
    oUrl = "http//www.",
    foundBy = FoundBy.Analyse,
    date = new Date(System.currentTimeMillis()),
    priority = 5,
    skills = List("A","B","C"),
    nFreelancers = Some(12))
  val foundJobsRow2 = FoundJobsRow(
    id = 1001L,
    oUrl = "http//www.",
    foundBy = FoundBy.Search,
    date = new Date(System.currentTimeMillis()),
    priority = 5,
    skills = List("A","B","C"),
    nFreelancers = Some(12))
  def jobsRow(fjr:FoundJobsRow):JobsRow = JobsRow(
    id = 1002L,
    foundData = fjr,
    daeDate = Some(new Date(System.currentTimeMillis())),
    deleteDate = Some(new Date(System.currentTimeMillis())),
    nextCheckDate = Some(new Date(System.currentTimeMillis())),
    jabData = Job(
      createDate = new Date(System.currentTimeMillis()),
      postDate = Some(new Date(System.currentTimeMillis())),
      deadline = Some(new Date(System.currentTimeMillis())),
      jobTitle = Some("Title"),
      jobType = Some("type"),
      jobPaymentType = Payment.Hourly,
      jobPrice = Some(12.3),
      jobEmployment = Employment.AsNeeded,
      jobLength = Some("10 m"),
      jobRequiredLevel = SkillLevel.Expert,
      jobQualifications = Map("a" -> "1","b" -> "2"),
      jobDescription = Some("Some job")))
  val jobsChangesRow = JobsChangesRow(
    id = 1003L,
    jobId = 1002L,
    changeData = JobChanges(
      createDate = new Date(System.currentTimeMillis()),
      jobAvailable = JobAvailable.No,
      lastViewed = Some(new Date(System.currentTimeMillis())),
      nApplicants = Some(2),
      applicantsAvg = Some(12.3),
      rateMin = Some(12.3),
      rateAvg = Some(12.3),
      rateMax = Some(12.3),
      nInterviewing = Some(321),
      interviewingAvg = Some(12.3),
      nHires = Some(321)))
  val clientsChangesRow = ClientsChangesRow(
    id = 10053L,
    jobId = 1002L,
    changeData = ClientChanges(
      createDate = new Date(System.currentTimeMillis()),
      name = Some("clientName"),
      logoUrl = Some("clientLogoUrl"),
      url = Some("clientUrl"),
      description = Some("clientDescription"),
      paymentMethod = PaymentMethod.Verified,
      rating = Some(12.3),
      nReviews = Some(321),
      location = Some("clientLocation"),
      time = Some("clientTime"),
      nJobs = Some(321),
      hireRate = Some(321),
      nOpenJobs = Some(321),
      totalSpend = Some(12.3),
      nHires = Some(321),
      nActive = Some(321),
      avgRate = Some(12.3),
      hours = Some(321),
      registrationDate = Some(new Date(System.currentTimeMillis()))),
   logo = Some(new BufferedImage(10,10,1)))
  val jobsApplicantsRow = JobsApplicantsRow(
    id = 1004L,
    jobId = 1002L,
    applicantData = JobApplicant(
      createDate = new Date(System.currentTimeMillis()),
      upDate = Some(new Date(System.currentTimeMillis())),
      name = Some("name"),
      initiatedBy = InitiatedBy.Client,
      url = Some("url")),
    freelancerId = Some(1006L))
  val jobsHiredRow = JobsHiredRow(
    id = 1005L,
    jobId = 1002L,
    hiredData = JobHired(
      createDate = new Date(System.currentTimeMillis()),
      name = Some("name"),
      freelancerUrl = Some("freelancerUrl")),
    freelancerId = None)
  val clientsWorksHistoryRow = ClientsWorksHistoryRow(
    id = 1006L,
    jobId = 1002L,
    workData = ClientWork(
      createDate = new Date(System.currentTimeMillis()),
      oUrl = Some("oUrl"),
      title = Some("title"),
      inProgress = JobState.InProcess,
      startDate = Some(new Date(System.currentTimeMillis())),
      endDate = Some(new Date(System.currentTimeMillis())),
      paymentType = Payment.Budget,
      billed = Some(12.3),
      hours = Some(321),
      rate = Some(12.3),
      freelancerFeedbackText = Some("freelancerFeedbackText"),
      freelancerFeedback = Some(12.3),
      freelancerName = Some("freelancerName"),
      freelancerUrl = Some("freelancerUrl"),
      clientFeedback = Some(12.3)),
    freelancerId = None)
  val foundFreelancerRow = FoundFreelancerRow(
    id = 1007L,
    oUrl = "FoundFreelancerRow",
    date = new Date(System.currentTimeMillis()),
    priority = 10)
  //Provider
  val dbProvider = new DBProvider
  //Tests
  "initialize" in {
    dbProvider.init("jdbc:mysql://127.0.0.1:3306", "root", "qwerty", "freelance_analytics")}
  "add to excavators_log table" in {
    val ct = System.currentTimeMillis()
    dbProvider.addLogMessage(new Date(ct), "error","m1","Some message 1")
    dbProvider.addLogMessage(new Date(ct), "info","m2","Some message 2")}
  "add to odesk_found_jobs table" in {
    dbProvider.addFoundJobsRow(foundJobsRow1)
    dbProvider.addFoundJobsRow(foundJobsRow2)}
  "add to odesk_jobs table" in {
    dbProvider.addJobsRow(jobsRow(foundJobsRow1))}
  "add to odesk_jobs_changes table" in {
    dbProvider.addJobsChangesRow(jobsChangesRow)}
  "add to odesk_client_changes table" in {
    dbProvider.addClientsChangesRow(clientsChangesRow)}
  "add to odesk_jobs_applicants table" in {
    dbProvider.addJobsApplicantsRow(jobsApplicantsRow)}
  "add to odesk_jobs_hired table" in {
    dbProvider.addJobsHiredRow(jobsHiredRow)}
  "add to odesk_clients_works_history table" in {
    dbProvider.addClientsWorksHistoryRow(clientsWorksHistoryRow)}
  "add to odesk_found_freelancers table" in {
    dbProvider.addFoundFreelancerRow(foundFreelancerRow)}
  "get set of last jobs URL" in {
    val tr1 = dbProvider.getSetOfLastJobsURLoFundBy(10, FoundBy.Search)
    assert(tr1.nonEmpty)
    assert(tr1.contains("http//www."))
    val tr2 = dbProvider.getSetOfLastJobsURLoFundBy(10, FoundBy.Analyse)
    assert(tr2.nonEmpty)
    assert(tr2.contains("http//www."))}
  "get N of old found jobs" in {
    val (tr, _) = dbProvider.getNOfOldFoundByJobs(10, FoundBy.Search)
    assert(tr.nonEmpty)
    assert(tr.map(_.oUrl).contains("http//www."))}
  "check existence url" in {
    assert(dbProvider.isJobScraped("http//www.") == true)
    assert(dbProvider.isJobScraped("http//www.deewdew") == false)}
  "stop" in {
    dbProvider.halt()}}






















































