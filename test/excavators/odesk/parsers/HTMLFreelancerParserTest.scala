package excavators.odesk.parsers

import org.scalatest._
import java.util.{Date, Locale}
import java.text.SimpleDateFormat
import util.structures._

/**
 * Test for HTMLFreelancerParser.
 * Created by CAB on 04.11.2014.
 */

class HTMLFreelancerParserTest extends WordSpecLike with Matchers {
  //Helpers
  val dateFormater = new SimpleDateFormat("yyyy.MMM.dd HH:mm:ss", Locale.ENGLISH)
  def getHtml(path:String):String = {
    val uri = getClass.getResource(path).toURI
    io.Source.fromFile(uri).mkString}
  //Parser
  val htmlParser = new HTMLFreelancerParser
  //Tests
  "Ivan_N_~015d8df4348d317d56.html" in { //https://www.odesk.com/users/Ivan_N_~015d8df4348d317d56
    val html = getHtml("html\\Ivan_N_~015d8df4348d317d56.html")
    //
    val tr = htmlParser.parseFreelancerProfile(html)
    //Freelancer changes data
    val ct = new Date().getTime
    assert(tr.changes.createDate.getTime <= ct && tr.changes.createDate.getTime > (ct - 1000))
    assert(tr.changes.name == Some("Ivan N."))
    assert(tr.changes.link == None)
    assert(tr.changes.title == Some("Ivan_N"))
    assert(tr.changes.profileAccess == Some("public"))
    assert(tr.changes.exposeFullName == None)
    assert(tr.changes.role == Some("contractor"))
    assert(tr.changes.emailVerified == Verified.Yes)
    assert(tr.changes.videoUrl == None)
    assert(tr.changes.isInviteInterviewAllowed == Allowed.Yes)
    assert(tr.changes.availability == FreelancerAvailable.FullTime)
    assert(tr.changes.availableAgain == None)
    assert(tr.changes.responsivenessScore == Some("not_enough_invites"))
    assert(tr.changes.overview.get.split(" ").take(3).mkString(" ") == "Certified java developer.")
    assert(tr.changes.location == Some("Minks, Belarus"))
    assert(tr.changes.timeZone == Some(1))
    assert(tr.changes.languages == List(FreelancerLanguage("English",Some(2),Verified.No)))
    assert(tr.changes.photoUrl == Some("https://odesk-prod-portraits.s3.amazonaws.com/Users:n_ivan:PortraitUrl_100?" +
      "AWSAccessKeyId=1XVAX3FNQZAFC9GJCFR2&Expires=2147483647&Signature=t18gk1%2FK9qGUgOGdU2fZ%2FRpPESo%3D&1415091955"))
    assert(tr.changes.rate == Some(17.22))
    assert(tr.changes.rentPercent == Some(10))
    assert(tr.changes.rating == Some(5.0))
    assert(tr.changes.allTimeJobs == Some(2))
    assert(tr.changes.allTimeHours == Some(2))
    assert(tr.changes.skills == List("java", "oracle-java-ee", "web-services-development", "play-framework",
      "agile-software-development", "automated-testing", "web-scraping", "xslt", "orm", "database-programming"))
    assert(tr.changes.companyUrl == None)
    assert(tr.changes.companyLogoUrl == None)
    //List[FreelancerWorkRecord]
    assert(tr.works.size == 2)
    val tw0 = tr.works(0)
    assert(tw0.jobKey == Some("MCvz%2FBa3TC50m2eYSC0ycHN0bzOgPtAcc5EQJcp%2BaE0%3D"))
    assert(tw0.paymentType == Payment.Budget)
    assert(tw0.status == Status.Closed)
    assert(tw0.startDate == Some(dateFormater.parse("2014.Aug.01 00:00:00")))
    assert(tw0.endDate == Some(dateFormater.parse("2014.Aug.01 00:00:00")))
    assert(tw0.fromFull == Some(dateFormater.parse("2014.Aug.01 00:00:00")))
    assert(tw0.toFull == Some(dateFormater.parse("2014.Aug.19 00:00:00")))
    assert(tw0.openingTitle == Some("Import XML Document Of Users To EJB Call"))
    assert(tw0.engagementTitle == Some("Import XML Document Of Users To EJB Call"))
    assert(tw0.skills == List("ejb", "java","jboss","xml"))
    assert(tw0.openAccess == Some("odesk"))
    assert(tw0.cnyStatus == None)
    assert(tw0.financialPrivacy == None)
    assert(tw0.isHidden == Hidden.No)
    assert(tw0.agencyName == None)
    assert(tw0.segmentationData == None)
    assert(tw0.asType == Some("40"))
    assert(tw0.totalHours == Some(0.0))
    assert(tw0.rate == Some(0.0))
    assert(tw0.totalCost == Some(126.0))
    assert(tw0.chargeRate == Some(140.0))
    assert(tw0.amount == Some(140.0))
    assert(tw0.totalHoursPrecise == Some(0.0))
    assert(tw0.costRate == Some(126.0))
    assert(tw0.totalCharge == Some(140.0))
    assert(tw0.ffScores == Map("Skills" -> 5,"Quality" -> 5,"Availability" -> 4,"Deadlines" -> 5,"Communication" -> 5,"Cooperation" -> 5)) //Map[String,Int],
    assert(tw0.ffIsPublic == Some("1"))
    assert(tw0.ffComment == None)
    assert(tw0.ffPrivatePoint == Some(10))
    assert(tw0.ffReasons == List(Some(11),Some(12),Some(13)))
    assert(tw0.ffResponse == None)
    assert(tw0.ffScore == Some(4.85))
    assert(tw0.cfScores == Map("Skills" -> 5,"Quality" -> 5,"Availability" -> 5,"Deadlines" -> 5,"Communication" -> 5,"Cooperation" -> 5))
    assert(tw0.cfIsPublic == Some("1"))
    assert(tw0.cfComment == Some("Thanks for the great work.&nbsp;&nbsp;Ivan was on time and available to answer questions.&nbsp;&nbsp;Code nicely structured and unit tests written"))
    assert(tw0.cfResponse == None)
    assert(tw0.cfScore == Some(5.00))
    assert(tw0.lpTitle == Some("Web user EJB"))
    assert(tw0.lpThumbnail == None)
    assert(tw0.lpIsPublic == Public.Yes)
    assert(tw0.lpDescription.get.split(" ").take(3).mkString(" ") == "Ejb remote client")
    assert(tw0.lpRecno == Some("517305105032445952"))
    assert(tw0.lpCatLevel1 == Some("Software Development"))
    assert(tw0.lpCatRecno == Some(21))
    assert(tw0.lpCatLevel2 == Some("Scripts & Utilities"))
    assert(tw0.lpCompleted == Some("1408406400000"))
    assert(tw0.lpLargeThumbnail == None)
    assert(tw0.lpUrl == None)
    assert(tw0.lpProjectContractLinkState == Some("active"))
    //List[FreelancerPortfolioRecord]
    assert(tr.portfolio.size == 1)
    val tp0 = tr.portfolio(0)
    assert(tp0.dataId == Some("517305105032445952"))
    assert(tp0.title == Some("Web user EJB"))
    assert(tp0.imgUrl == None)
    //List[FreelancerTestRecord]
    assert(tr.tests.size == 2)
    val tt0 = tr.tests(0)
    assert(tt0.detailsUrl == Some("/exams/English-Spelling-U.S.-Version-Professionals_9747547"))
    assert(tt0.title == Some("English Spelling Test (U.S. Version)"))
    assert(tt0.score == Some(4.75))
    assert(tt0.timeComplete == Some(13))
    //List[FreelancerCertification]
    assert(tr.certification.size == 1)
    val tc0 = tr.certification(0)
    assert(tc0.rid == Some("522341295918956544"))
    assert(tc0.name == Some("Oracle Certified Professional - Java"))
    assert(tc0.customData == None)
    assert(tc0.score == None)
    assert(tc0.logoUrl == Some("/images/certification/logos/certification-logo-oracle-certified-professional.gif"))
    assert(tc0.certUrl == None)
    assert(tc0.isCertVerified == Some("1"))
    assert(tc0.isVerified == Some("1"))
    assert(tc0.description.get.split(" ").take(3).mkString(" ") == "Demonstrates the core")
    assert(tc0.provider == None)
    assert(tc0.skills == List("java"))
    assert(tc0.dateEarned == None)
    //List[FreelancerEmployment]
    assert(tr.employment.size == 2)
    val tem0 = tr.employment(0)
    assert(tem0.recordId == Some("2727863"))
    assert(tem0.title == Some("Software Developer"))
    assert(tem0.company == Some("Epam"))
    assert(tem0.dateFrom == Some(new Date(1341100800000L)))
    assert(tem0.dateTo == Some(new Date(1398902400000L)))
    assert(tem0.role == Some("Individual Contributor"))
    assert(tem0.companyCountry == Some("Belarus"))
    assert(tem0.companyCity == Some("Minsk"))
    assert(tem0.description.get.split(" ").take(3).mkString(" ") == "<span class=\"jsTruncated\">Project/Customer: Trizetto<br")
    //List[FreelancerEducation]
    assert(tr.education.size == 1)
    val ted0 = tr.education(0)
    assert(ted0.school == Some("Belarusian National Technical University"))
    assert(ted0.areaOfStudy == Some("Mechanical engineering"))
    assert(ted0.degree == Some("Master's degree"))
    assert(ted0.dateFrom == Some(dateFormater.parse("2004.Jan.01 00:00:00")))
    assert(ted0.dateTo == Some(dateFormater.parse("2009.Jan.01 00:00:00")))
    assert(ted0.comments == None)
    //List[FreelancerOtherExperience]
    assert(tr.experience.size == 0)}
  "Ivan_N_~015d8df4348d317d56.html/Job1.json" in {
    val json = getHtml("html\\Job1.json")
    //
    val otr = htmlParser.parseJobJson(json)
    val tr = otr
    //
    val ct = new Date().getTime
    assert(tr.createDate.getTime <= ct && tr.createDate.getTime > (ct - 1000))
    assert(tr.rawJson.split("\"").take(3).mkString("\"") == "{\"client\":{")
    assert(tr.jobContractorTier == None)
    assert(tr.jobSkills == List("ejb","java","jboss","xml"))
    assert(tr.jobUrl == Some("/jobs/~0172db7fc65ddb6a50"))
    assert(tr.jobIsPublic == Public.Yes)
    assert(tr.jobDescription.get.split(" ").take(3).mkString(" ") == "We have some")
    assert(tr.jobCategory == Some("Other - Software Development"))
    assert(tr.jobEndDate == Some(dateFormater.parse("2014.Aug.21 00:00:00")))
    assert(tr.jobEngagement == Some("Full-time - 30+ hrs/week"))
    assert(tr.jobDuration == None)
    assert(tr.jobAmount == Some(150.0))
    assert(tr.clientTotalFeedback == Some(17))
    assert(tr.clientScore == Some(4.76932314193289))
    assert(tr.clientTotalCharge == Some(6601.85))
    assert(tr.clientTotalHires == Some(30))
    assert(tr.clientActiveContract == Some(3))
    assert(tr.clientCountry == Some("New Zealand"))
    assert(tr.clientCity == Some("Auckland"))
    assert(tr.clientTime == Some("7:50 AM (UTC)"))
    assert(tr.clientMemberSince == Some(dateFormater.parse("2012.Mar.24 00:00:00")))
    assert(tr.clientProfileLogo == None)
    assert(tr.clientProfileName == Some("Flip Desk"))
    assert(tr.clientProfileUrl == None)
    assert(tr.clientProfileSummary == None)}
  "Ivan_N_~015d8df4348d317d56.html/Portfolio1.json" in {
    val json = getHtml("html\\Portfolio1.json")
    //
    val otr = htmlParser.parsePortfolioJson(json)
    val tr = otr
    //
    val ct = new Date().getTime
    assert(tr.createDate.getTime <= ct && tr.createDate.getTime > (ct - 1000))
    assert(tr.rawJson.split("\"").take(3).mkString("\"") == "{\"code\":200,")
    assert(tr.projectDate == Some(dateFormater.parse("2014.Aug.19 00:00:00")))
    assert(tr.title == Some("Web user EJB"))
    assert(tr.description.get.split(" ").take(3).mkString(" ") == "Ejb remote client")
    assert(tr.imgUrl == None)
    assert(tr.isPublic == Public.Yes)
    assert(tr.attachments == List())
    assert(tr.creationTs == Some(dateFormater.parse("2014.Oct.01 13:28:42")))
    assert(tr.category == Some("Software Development"))
    assert(tr.subCategory == Some("Scripts & Utilities"))
    assert(tr.skills == List("XSLT","Java"))
    assert(tr.isClient == Client.No)
    assert(tr.flagComment == None)
    assert(tr.projectUrl == None)}}
