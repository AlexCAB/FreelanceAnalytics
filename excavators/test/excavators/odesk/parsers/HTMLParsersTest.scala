package excavators.odesk.parsers

import org.scalatest._
import java.util.Locale
import java.text.SimpleDateFormat
import excavators.odesk.structures._
import scala.Some

/**
 * Test for HTMLParsers object
 * Created by CAB on 16.09.14.
 */

class HTMLParsersTest extends WordSpecLike with Matchers {
  //Helpers
  val dateFormater = new SimpleDateFormat("yyyy.MMM.dd HH:mm:ss", Locale.ENGLISH)
  def getHtml(path:String):String = {
    val uri = getClass.getResource(path).toURI
    io.Source.fromFile(uri).mkString}
  //Parser
  val htmlParser = new HTMLParsers
  //Tests
  "HTMLParsers must:" must{
    "parseWorkSearchResult" in {
      val html = getHtml("html\\SearchResult.html")
      //
      val lw = htmlParser.parseWorkSearchResult(html)
      //
      assert(lw.works.size == 10)
      assert(lw.works(0) == FoundWork(
        url = "/jobs/IOS-and-Android-Tour-App_%7E015374e4dd7938042f",
        skills = List("android-app-development", "apple-xcode", "core-java", "eclipse", "iphone-app-development", "jquery-mobile", "objective-c", "phonegap"),
        nFreelancers = None))
      assert(lw.works(1) == FoundWork(
        url = "/jobs/09162014_173364_Translation_French_350_%7E012efa5e628d98922f",
        skills = List(),
        nFreelancers = None))
      assert(lw.works(9) == FoundWork(
        url = "/jobs/Write-articles-about-Financial-planning_%7E012df1b84c43dca7d8",
        skills = List("article-writing", "blog-writing", "financial-management", "insurance-consulting"),
        nFreelancers = Some(3)))
      assert(lw.nFound == Some(74756))
      assert(lw.nextUrl == Some("/jobs/?q=&skip=10"))}
    "parse hourly job" in {//https://www.odesk.com/jobs/WordPress-Plugin-Developer-Possible-Long-Term-Contract_~0116a80a41a3bd221a
      val html = getHtml("html\\JobHourly.html")
      //
      val tr = htmlParser.parseJob(html).get
      //
      val ct = System.currentTimeMillis()
      //job:Job
      val fd1 = tr.job.date.getTime
      assert((fd1 < ct && fd1 > (ct - 1000)) == true)
      val pd = tr.job.postDate.get.getTime + (1000 * 60 * 29) //Posted  29 minutes ago
      assert((pd < ct && pd > (ct - 1000)) == true)
      assert(tr.job.jobTitle == Some("WordPress Plugin Developer - Possible Long Term Contract"))
      assert(tr.job.jobType == Some("Web Programming"))
      assert(tr.job.jobPaymentType == Payment.Hourly)
      assert(tr.job.jobPrice == None)
      assert(tr.job.jobEmployment == Employment.AsNeeded)
      assert(tr.job.jobLength == Some("Less than 1 month"))
      assert(tr.job.jobRequiredLevel == SkillLevel.Expert)
      assert(tr.job.jobQualifications == Map(
        "Feedback Score:" -> "At least 4.50 ",
        "English Level:" -> "Fluent - Has complete command of this language with perfect grammar ",
        "oDesk Hours:" -> "At least 100 hours "))
      assert(tr.job.jobDescription.get.split(" ").take(4).mkString(" ") == "<section id=\"jobDescriptionSection\"> \n <h1")
      //changes:JobChanges
      val fd2 = tr.changes.date.getTime
      assert((fd2 < ct &&  fd2 > (ct - 1000)) == true)
      assert(tr.changes.lastViewed == None)
      assert(tr.changes.jobAvailable == JobAvailable.Yes)
      assert(tr.changes.nApplicants == Some(46))
      assert(tr.changes.rateMin == None)
      assert(tr.changes.rateAvg == None)
      assert(tr.changes.rateMax == None)
      assert(tr.changes.interviewing == Some(1))
      assert(tr.changes.nHires == None)
      assert(tr.changes.clientName == None)
      assert(tr.changes.clientLogoUrl == None)
      assert(tr.changes.clientUrl == None)
      assert(tr.changes.clientDescription == None)
      assert(tr.changes.clientPaymentMethod == PaymentMethod.Verified)
      assert(tr.changes.clientRating == Some(5.0))
      assert(tr.changes.clientNReviews == Some(1))
      assert(tr.changes.clientLocation == Some("United Kingdom London"))
      assert(tr.changes.clientNJobs == Some(7))
      assert(tr.changes.clientHireRate == Some(58))
      assert(tr.changes.clientNOpenJobs == Some(1))
      assert(tr.changes.clientTotalSpend == Some(686.0))
      assert(tr.changes.clientNHires == Some(3))
      assert(tr.changes.clientNActive == Some(0))
      assert(tr.changes.clientAvgRate == Some(25.0))
      assert(tr.changes.clientHours == Some(23))
      assert(tr.changes.clientRegistrationDate == Some(dateFormater.parse("2012.Mar.21 00:00:00")))
      //applicants:List[JobApplicant]
      assert(tr.applicants.size == 47)
      val a1 = tr.applicants(0)
      val fd3 = a1.date.getTime
      assert((fd3 < ct &&  fd3 > (ct - 1000)) == true)
      val ud = a1.upDate.get.getTime + (1000 * 60 * 5) //8 minutes ago
      assert((ud < ct &&  ud > (ct - 1000)) == true)
      assert(a1.name == Some("James C."))
      assert(a1.initiatedBy == InitiatedBy.Freelancer)
      assert(a1.url == Some("/users/Wordpress-Woocommerce-Magento-Expert_%7E01121e98f92f9ed89d"))
      val a2 = tr.applicants(1)
      assert(a2.name == Some("Kavish Rathore"))
      assert(a2.initiatedBy == InitiatedBy.Freelancer)
      assert(a2.url == None)
      //hires:List[JobHired]
      assert(tr.hires.size == 0)
      //clientWorks:List[ClientWork]
      assert(tr.clientWorks.size == 3)
      val w0 = tr.clientWorks(0)
      val fd4 = w0.date.getTime
      assert((fd4 < ct &&  fd4 > (ct - 1000)) == true)
      assert(w0.oUrl == None)
      assert(w0.title == Some("WordPress Plugin/Theme Development - Long Term"))
      assert(w0.inProgress == JobState.InProcess)
      assert(w0.startDate == Some(dateFormater.parse("2014.Jun.01 00:00:00")))
      assert(w0.endDate == None)
      assert(w0.paymentType == Payment.Hourly)
      assert(w0.billed == Some(316.66))
      assert(w0.hours == Some(15))
      assert(w0.rate == Some(25.0))
      assert(w0.freelancerFeedbackText == None)
      assert(w0.freelancerFeedback == None)
      assert(w0.freelancerName == Some("Vasile G."))
      assert(w0.freelancerUrl == Some("/users/Senior-Web-Developer-PHP-MySQL-JavaScript_%7E01d91426ff617b810c"))
      assert(w0.clientFeedback == None)
      val w2 = tr.clientWorks(2)
      assert(w2.oUrl == None)
      assert(w2.title == Some("WordPress Plugin Development"))
      assert(w2.inProgress == JobState.End)
      assert(w2.startDate == Some(dateFormater.parse("2012.Mar.01 00:00:00")))
      assert(w2.endDate == Some(dateFormater.parse("2012.Apr.01 00:00:00")))
      assert(w2.paymentType == Payment.Budget)
      assert(w2.billed == Some(111.11))
      assert(w2.hours == None)
      assert(w2.rate == None)
      assert(w2.freelancerFeedbackText.get.split(" ").take(4).mkString(" ") == "I've had a great")
      assert(w2.freelancerFeedback == Some(5.0))
      assert(w2.freelancerName == Some("Aurel Canciu"))
      assert(w2.freelancerUrl == Some("/users/Full-stack-web-developer-Ruby-Rails_%7E0164f7e0113dd91b16"))
      assert(w2.clientFeedback == Some(5.0))}
    "parse budget job" in { //https://www.odesk.com/jobs/Display-ToolTip-SharePoint-2013-DVW-controls_~01b8509ad36fb242d1
      val html = getHtml("html\\JobBudget.html")
      //
      val tr = htmlParser.parseJob(html).get
      //
      val ct = System.currentTimeMillis()
      //job:Job
      val pd = tr.job.postDate.get.getTime + (1000 * 60 * 60 * 9) //Posted  9 hours ago
      assert((pd < ct && pd > (ct - 1000)) == true)
      assert(tr.job.jobPaymentType == Payment.Budget)
      assert(tr.job.jobPrice == Some(200.0))
      assert(tr.job.jobEmployment == Employment.Unknown)
      assert(tr.job.jobLength == None)
      assert(tr.job.jobRequiredLevel == SkillLevel.Unknown)
      assert(tr.job.jobQualifications == Map())
      assert(tr.job.jobDescription.get.split(" ").take(4).mkString(" ") == "<section id=\"jobDescriptionSection\"> \n <h1")
      //changes:JobChanges
      val lw = tr.changes.lastViewed.get.getTime + (1000 * 60 * 60 * 10) //10 hours ago
      assert((lw < ct && lw > (ct - 1000)) == true)
      assert(tr.changes.nApplicants == Some(4))
      assert(tr.changes.jobAvailable == JobAvailable.Yes)
      assert(tr.changes.applicantsAvg == Some(156.67))
      assert(tr.changes.rateMin == None)
      assert(tr.changes.rateAvg == None)
      assert(tr.changes.rateMax == None)
      assert(tr.changes.interviewing == Some(0))
      assert(tr.changes.interviewingAvg == None)
      assert(tr.changes.nHires == None)
      assert(tr.changes.clientName == None)
      assert(tr.changes.clientLogoUrl == None)
      assert(tr.changes.clientUrl == None)
      assert(tr.changes.clientDescription == None)
      assert(tr.changes.clientPaymentMethod == PaymentMethod.No)
      assert(tr.changes.clientRating == None)
      assert(tr.changes.clientNReviews == None)
      assert(tr.changes.clientLocation == Some("India Mumbai"))
      assert(tr.changes.clientNJobs == Some(3))
      assert(tr.changes.clientHireRate == Some(0))
      assert(tr.changes.clientNOpenJobs == Some(1))
      assert(tr.changes.clientTotalSpend == None)
      assert(tr.changes.clientNHires == None)
      assert(tr.changes.clientNActive == None)
      assert(tr.changes.clientAvgRate == None)
      assert(tr.changes.clientHours == None)
      assert(tr.changes.clientRegistrationDate == Some(dateFormater.parse("2013.Jun.23 00:00:00")))
      //applicants:List[JobApplicant]
      assert(tr.applicants.size == 4)
      val a1 = tr.applicants(0)
      val ud = a1.upDate.get.getTime + (1000 * 60 * 39) //about an hour ago
      assert((ud < ct &&  ud > (ct - 1000)) == true)
      assert(a1.name == Some("Kailash Bokade"))
      assert(a1.initiatedBy == InitiatedBy.Freelancer)
      assert(a1.url == Some("/users/SharePoint-ASP-Net-Developer_%7E01699a692bf89d69bc"))
      //hires:List[JobHired]
      assert(tr.hires.size == 0)
      //clientWorks:List[ClientWork]
      assert(tr.clientWorks.size == 0)}
    "parse applicants rich info " in {  //https://www.odesk.com/jobs/FACEBOOK-AUTO-POSTER-SCRIPT_~011464d760e8e4eeff
      val html = getHtml("html\\JobRichInfo.html")
      //
      val tr = htmlParser.parseJob(html).get
      //
      val ct = System.currentTimeMillis()
      //job:Job
      val pd = tr.job.postDate.get.getTime + (1000 * 60 * 60 * 24) //Posted  1 day ago
      assert((pd < ct && pd > (ct - 1000)) == true)
      //changes:JobChanges
      assert(tr.changes.rateMin == Some(30.00))
      assert(tr.changes.rateAvg == Some(109.32))
      assert(tr.changes.rateMax == Some(333.33))
      assert(tr.changes.clientTime == Some("05:41 PM"))
      //applicants:List[JobApplicant]
      assert(tr.applicants.size == 10)
      //hires:List[JobHired]
      assert(tr.hires.size == 0)
      //clientWorks:List[ClientWork]
      assert(tr.clientWorks.size == 0)}
    "parse closed job and hired info and client company info" in {//https://www.odesk.com/jobs/~019ba335f65a0f5ace
      val html = getHtml("html\\JobClosed.html")
      //
      val tr = htmlParser.parseJob(html).get
      //
      val ct = System.currentTimeMillis()
      //job:Job
      assert(tr.job.postDate == Some(dateFormater.parse("2013.Jan.11 00:00:00")))
      assert(tr.job.jobType == Some("Video Production"))
      //changes:JobChanges
      assert(tr.changes.jobAvailable == JobAvailable.No)
      assert(tr.changes.lastViewed == Some(dateFormater.parse("2013.Jan.28 00:00:00")))
      assert(tr.changes.nApplicants == Some(55))
      assert(tr.changes.rateMin == None)
      assert(tr.changes.rateAvg == None)
      assert(tr.changes.rateMax == None)
      assert(tr.changes.interviewing == Some(1))
      assert(tr.changes.nHires == None)
      assert(tr.changes.clientName == Some("Crush Design"))
      assert(tr.changes.clientLogoUrl == Some(
        "https://odesk-prod-portraits.s3.amazonaws.com/Companies:462279:CompanyLogoURL?" +
        "AWSAccessKeyId=1XVAX3FNQZAFC9GJCFR2&Expires=2147483647&Signature=McU0zrMt5ydaHpmBFHEPlliTzQk%3D"))
      assert(tr.changes.clientUrl == Some("http://www.crush-design.co.uk"))
      assert(tr.changes.clientDescription == Some("Crush Design http://www.crush-design.co.uk Design and Marketing Agency"))
      assert(tr.changes.clientPaymentMethod == PaymentMethod.Verified)
      assert(tr.changes.clientRating == Some(4.92))
      assert(tr.changes.clientNReviews == Some(9))
      assert(tr.changes.clientLocation == Some("United Kingdom Chesterfield"))
      assert(tr.changes.clientTime == Some("10:33 AM"))
      assert(tr.changes.clientNJobs == Some(25))
      assert(tr.changes.clientHireRate == Some(44))
      assert(tr.changes.clientNOpenJobs == Some(1))
      assert(tr.changes.clientTotalSpend == Some(10000.0))
      assert(tr.changes.clientNHires == Some(9))
      assert(tr.changes.clientNActive == Some(0))
      assert(tr.changes.clientAvgRate == Some(8.62))
      assert(tr.changes.clientHours == Some(1554))
      assert(tr.changes.clientRegistrationDate == Some(dateFormater.parse("2011.Dec.07 00:00:00")))
      //applicants:List[JobApplicant]
      assert(tr.applicants.size == 56)
      //hires:List[JobHired]
      assert(tr.hires.size == 1)
      val h = tr.hires(0)
      val fd2 = h.date.getTime
      assert((fd2 < ct &&  fd2 > (ct - 1000)) == true)
      assert(h.name == Some("Rolando Salazar"))
      assert(h.freelancerUrl == Some("/users/%7E013f14399977ee965e"))
      //clientWorks:List[ClientWork]
      assert(tr.clientWorks.size == 9)
      val w0 = tr.clientWorks(0)
      assert(w0.oUrl == Some("/jobs/Front-End-Development-one-bespoke-landing-page-from-PSD_%7E01873a013776ffb0e2"))
      assert(w0.title == Some("Front End Development of one bespoke landing page from PSD"))}










  }






}









































