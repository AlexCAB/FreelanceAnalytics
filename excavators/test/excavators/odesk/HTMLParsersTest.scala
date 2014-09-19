package excavators.odesk
import org.scalatest._
import excavators.helpers.TestLogger
import java.net.URL
import java.util.{Calendar, Locale, Date}
import java.text.SimpleDateFormat

/**
 * Test for HTMLParsers object
 * Created by CAB on 16.09.14.
 */

class HTMLParsersTest extends WordSpecLike with Matchers {
  "HTMLParsers must:" must{
    "parseWorkSearchResult" in {
      val htmlParser = new HTMLParsers
      val uri = getClass.getResource("html\\SearchResult.html").toURI
      val html = io.Source.fromFile(uri).mkString
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
    "parse hourly job" in {
    val htmlParser = new HTMLParsers
    val uri = getClass.getResource("html\\JobHourly.html").toURI //https://www.odesk.com/jobs/WordPress-Plugin-Developer-Possible-Long-Term-Contract_~0116a80a41a3bd221a
    val html = io.Source.fromFile(uri).mkString
    val df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.ENGLISH)
    //
    val tr = htmlParser.parseJob(html).get
    //
    val ct = System.currentTimeMillis()
    //job:Job
    val fd1 = tr.job.date.getTime
    assert((fd1 < ct && fd1 > (ct - 1000)) == true)
    val pd = tr.job.postDate.get.getTime + (1000 * 60 * 29) //Posted  29 minutes ago
    assert((pd < ct && pd > (ct - 1000)) == true)
    assert(tr.job.daeDate == None)
    assert(tr.job.jobTitle == Some("WordPress Plugin Developer - Possible Long Term Contract"))
    assert(tr.job.jobType == Some("Web Programming"))
    assert(tr.job.jobPaymentType == Payment.Hourly)
    assert(tr.job.jobPrice == None)
    assert(tr.job.jobEmployment == Employment.AsNeeded)
    assert(tr.job.jobLength == Some("Less than 1 month"))
    assert(tr.job.jobRequiredLevel == SkillLevel.Expert)
    assert(tr.job.jobDescription.get.split(" ").take(4).mkString(" ") == "<section id=\"jobDescriptionSection\"> \n <h1")
    //changes:JobChanges
    val fd2 = tr.changes.date.getTime
    assert((fd2 < ct &&  fd2 > (ct - 1000)) == true)
    assert(tr.changes.lastViewed == None)
    assert(tr.changes.nApplicants == Some(46))
    assert(tr.changes.rateMin == None)
    assert(tr.changes.rateAvg == None)
    assert(tr.changes.rateMax == None)
    assert(tr.changes.interviewing == Some(1))
    assert(tr.changes.clientDescription == None)
    assert(tr.changes.clientPaymentMethod == PaymentMethod.Verified)
    assert(tr.changes.clientRating == Some(5.0))
    assert(tr.changes.clientNReviews == Some(1))
    assert(tr.changes.clientLocation == Some("United Kingdom"))
    assert(tr.changes.clientNJobs == Some(7))
    assert(tr.changes.clientHireRate == Some(58))
    assert(tr.changes.clientNOpenJobs == Some(1))
    assert(tr.changes.clientTotalSpend == Some(686.0))
    assert(tr.changes.clientNHires == Some(3))
    assert(tr.changes.clientNActive == Some(0))
    assert(tr.changes.clientAvgRate == Some(25.0))
    assert(tr.changes.clientHours == Some(23))
    assert(tr.changes.clientRegistrationDate == Some(df.parse("2012.Mar.21 00:00:00")))
    //applicants:List[JobApplicant]
    assert(tr.applicants.size == 46)
    val a1 = tr.applicants(0)
    val fd3 = a1.date.getTime
    assert((fd3 < ct &&  fd3 > (ct - 1000)) == true)
    val ud = a1.upDate.get.getTime + (1000 * 60 * 8) //8 minutes ago
    assert((ud < ct &&  ud > (ct - 1000)) == true)
    assert(a1.name == Some("James C."))
    assert(a1.initiatedBy == InitiatedBy.Freelancer)
    assert(a1.url == Some("https://www.odesk.com/users/Wordpress-Woocommerce-Magento-Expert_~01121e98f92f9ed89d"))
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
    assert(w0.startDate == Some(df.parse("2014.Jun.01 00:00:00")))
    assert(w0.endDate == None)
    assert(w0.paymentType == Payment.Hourly)
    assert(w0.billed == Some(316.66))
    assert(w0.hours == Some(15))
    assert(w0.rate == Some(25.0))
    assert(w0.freelancerFeedbackText == None)
    assert(w0.freelancerFeedback == None)
    assert(w0.freelancerName == Some("Vasile G."))
    assert(w0.freelancerUrl == Some("https://www.odesk.com/users/Senior-Web-Developer-PHP-MySQL-JavaScript_~01d91426ff617b810c"))
    assert(w0.clientFeedback == None)
    val w2 = tr.clientWorks(2)
    assert(w2.oUrl == None)
    assert(w2.title == Some("WordPress Plugin Development"))
    assert(w2.inProgress == JobState.End)
    assert(w2.startDate == Some(df.parse("2012.Mar.01 00:00:00")))
    assert(w2.endDate == Some(df.parse("2012.Apr.01 00:00:00")))
    assert(w2.paymentType == Payment.Budget)
    assert(w2.billed == Some(111.11))
    assert(w2.hours == None)
    assert(w2.rate == None)
    assert(w2.freelancerFeedbackText.get.split(" ").take(4).mkString(" ") == Some("I've had a great"))
    assert(w2.freelancerFeedback == Some(5.0))
    assert(w2.freelancerName == Some("Aurel Canciu"))
    assert(w2.freelancerUrl == Some("https://www.odesk.com/users/Full-stack-web-developer-Ruby-Rails_~0164f7e0113dd91b16"))
    assert(w2.clientFeedback == Some(5.0))}

//    "parse budget job" in {
//      val htmlParser = new HTMLParsers(new TestLogger)
//      val uri = getClass.getResource("html\\JobHourly.html").toURI //https://www.odesk.com/jobs/WordPress-Plugin-Developer-Possible-Long-Term-Contract_~0116a80a41a3bd221a
//      val html = io.Source.fromFile(uri).mkString
//      val df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.ENGLISH)
//      //
//      val tr = htmlParser.parseJob(html)
//      //
//      val ct = System.currentTimeMillis()
//      //job:Job
//      val fd = tr.job.foundDate.getTime
//      assert(fd < ct &&  fd > (ct - 1000))
//      val pd = tr.job.postDate.get.getTime + (1000 * 60 * 32) //Posted  32 minutes ago
//      assert(pd < ct &&  pd > (ct - 1000))
//      assert(tr.job.daeDate == Some(df.parse("2014.Nov.15 00:00:00")))
//      assert(tr.job.deleteDate == None)
//      assert(tr.job.nextCheckDate == None)
//      assert(tr.job.jobTitle == Some("Native British English Copywriter for a Mechanical Engineering Website"))
//      assert(tr.job.jobType == Some("Website Content"))
//      assert(tr.job.jobPaymentType == Payment.Budget)
//      assert(tr.job.jobPrice == Some(2800.0))
//      assert(tr.job.jobEmployment == Employment.Unknown)
//      assert(tr.job.jobLength == None)
//      assert(tr.job.jobRequiredLevel == SkillLevel.Unknown)
//      assert(tr.job.jobDescription.get.split(" ").take(3).mkString(" ") == "We are currently")
//      //changes:JobChanges
//
//
//
//      //applicants:List[JobApplicant]
//
//
//
//      //hires:List[JobHired]
//
//
//
//      //clientWorks:List[ClientWork]
//
//
//
//
//
//
//
//
//
//    }

    "parse closed job" in {

         //https://www.odesk.com/jobs/~019ba335f65a0f5ace

    }

  }






}









































