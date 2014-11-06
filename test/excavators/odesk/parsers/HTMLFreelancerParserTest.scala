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
    assert(tr.changes.emailVerified == Some(true))
    assert(tr.changes.videoUrl == None)
    assert(tr.changes.isInviteInterviewAllowed == Some(true))
    assert(tr.changes.availability == FreelancerAvailable.FullTime)
    assert(tr.changes.availableAgain == None)
    assert(tr.changes.responsivenessScore == Some("not_enough_invites"))
    assert(tr.changes.overview.get.split(" ").take(3).mkString(" ") == "Certified java developer.")
    assert(tr.changes.location == Some("Minks, Belarus"))
    assert(tr.changes.timeZone == Some(1))
    assert(tr.changes.languages == List(FreelancerLanguage("English",Some(2),Some(false))))
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
    assert(tw0.startDate == Some(dateFormater.format("2014.Aug.01 00:00:00")))
    assert(tw0.endDate == Some(dateFormater.format("2014.Aug.01 00:00:00")))
    assert(tw0.fromFull == Some(dateFormater.format("2014.Aug.01 00:00:00")))
    assert(tw0.toFull == Some(dateFormater.format("2014.Aug.19 00:00:00")))
    assert(tw0.openTitle == Some("Import XML Document Of Users To EJB Call"))
    assert(tw0.endTitle == Some("Import XML Document Of Users To EJB Call"))
    assert(tw0.skills == List("ejb", "java","jboss","xml"))
    assert(tw0.openAccess == Access.Pubic)
    assert(tw0.cnyStatus == None)
    assert(tw0.financialPrivacy == None)
    assert(tw0.isHidden == Some(false))
    assert(tw0.agencyName == None)
    assert(tw0.segmentationData == None)
    assert(tw0.asType == Some("40"))
    assert(tw0.totalHours == Some(0.0))
    assert(tw0.rate == Some(0.0))
    assert(tw0.totalCost == Some(126.0))
    assert(tw0.chargeRate == Some(140.0))
    assert(tw0.amount == Some(140.0))
    assert(tw0.totalHoursPrecise == Some(0.0))
    assert(tw0.blendedRate == Some(126.0))
    assert(tw0.totalCharge == Some(140.0))
    assert(tw0.ffScores == Map("Skills" -> 5,"Quality" -> 5,"Availability" -> 4,"Deadlines" -> 5,"Communication" -> 5,"Cooperation" -> 5)) //Map[String,Int],
    assert(tw0.ffIsPublic == Some("1"))
    assert(tw0.ffComment == None)
    assert(tw0.ffPrivatePoint == Some(10))
    assert(tw0.ffReasons == List(11,12,13))
    assert(tw0.ffResponse == None)
    assert(tw0.ffScore == Some(4.85))
    assert(tw0.cfScores == Map("Skills" -> 5,"Quality" -> 5,"Availability" -> 5,"Deadlines" -> 5,"Communication" -> 5,"Cooperation" -> 5))
    assert(tw0.cfIsPublic == Some("1"))
    assert(tw0.cfComment == Some("Thanks for the great work.&nbsp;&nbsp;Ivan was on time and available to answer questions.&nbsp;&nbsp;Code nicely structured and unit tests written"))
    assert(tw0.cfResponse == None)
    assert(tw0.cfScore == Some(5.00))






  }
//  "Creator-Software-Made-order_~01aae5df549caa4620" in { //https://www.odesk.com/users/Creator-Software-Made-order_~01aae5df549caa4620
//    val html = getHtml("html\\Creator-Software-Made-order_~01aae5df549caa4620.html")
//    //
//    val tr = htmlParser.parseFreelancerProfile(html)
//    //Freelancer changes data
//    assert(tr.changes.name == Some("Applicius SAS"))
//    assert(tr.changes.link == None)
//    assert(tr.changes.title == Some("Creator of Software &quot;Made to order&quot;"))
//    assert(tr.changes.profileAccess == Some("public"))
//    assert(tr.changes.exposeFullName == None)
//    assert(tr.changes.role == Some("contractor"))
//    assert(tr.changes.emailVerified == Some(true))
//    assert(tr.changes.videoUrl == None)
//    assert(tr.changes.isInviteInterviewAllowed == Some(true))
//    assert(tr.changes.availability == FreelancerAvailable.Unknown)
//    assert(tr.changes.availableAgain == None)
//    assert(tr.changes.responsivenessScore == None)
//    assert(tr.changes.overview.get.split(" ").take(2).mkString(" ") == "GitHub organization:")
//    assert(tr.changes.location == Some("Paris, France"))
//    assert(tr.changes.timeZone == Some(-1))
//    assert(tr.changes.languages == List(FreelancerLanguage("English",Some(2),Some(false))))
//    assert(tr.changes.photoUrl == Some("https://odesk-prod-portraits.s3.amazonaws.com/Users:applicius:PortraitUrl_100?" +
//      "AWSAccessKeyId=1XVAX3FNQZAFC9GJCFR2&Expires=2147483647&Signature=WjuKJ%2B5A87mes9CmU4KYqibEKpU%3D&1415091989"))
//    assert(tr.changes.rate == Some(100.0))
//    assert(tr.changes.rentPercent == Some(10))
//    assert(tr.changes.rating == None)
//    assert(tr.changes.allTimeJobs == None)
//    assert(tr.changes.allTimeHours == None)
//    assert(tr.changes.skills == List("scala", "play-framework", "haskell", "functional-testing", "java"))
//    assert(tr.changes.companyUrl == Some("/companies/Applicius_~01a930e032cd85f14a"))
//    assert(tr.changes.companyLogoUrl == Some("https://odesk-prod-portraits.s3.amazonaws.com/Companies:1295078:CompanyLo" +
//      "goURL?AWSAccessKeyId=1XVAX3FNQZAFC9GJCFR2&Expires=2147483647&Signature=gCL7FiHRqy4MBcWBWPzOZxWKL%2FU%3D"))
//    //
//
//  }
//  "Maths-Phd-Java-Scala-Software-Engineer_~019c948a9320f6e9e9" in { //https://www.odesk.com/users/Maths-Phd-Java-Scala-Software-Engineer_~019c948a9320f6e9e9
//    val html = getHtml("html\\Maths-Phd-Java-Scala-Software-Engineer_~019c948a9320f6e9e9.html")
//    //
//    //    val lw = htmlParser.parseWorkSearchResult(html)
//    //    //
//    //      assert(lw.works.size == 10)
//
//  }
//  "Scala-trainer-consultant_~01963f82fbf707f2d0" in { //https://www.odesk.com/users/Scala-trainer-consultant_~01963f82fbf707f2d0
//    val html = getHtml("html\\Scala-trainer-consultant_~01963f82fbf707f2d0.html")
//    //
//    //    val lw = htmlParser.parseWorkSearchResult(html)
//    //    //
//    //      assert(lw.works.size == 10)
//
//  }
//  "Senior-Scala-Java-Android-iOS-Developer_~013a6f352dc7034896" in { //https://www.odesk.com/users/Senior-Scala-Java-Android-iOS-Developer_~013a6f352dc7034896
//    val html = getHtml("html\\Senior-Scala-Java-Android-iOS-Developer_~013a6f352dc7034896.html")
//    //
//    //    val lw = htmlParser.parseWorkSearchResult(html)
//    //    //
//    //      assert(lw.works.size == 10)
//
//  }
//  "Java-Scala-Clojure-JavaScript-expert_~0164f352a473869d64" in { //https://www.odesk.com/users/Java-Scala-Clojure-JavaScript-expert_~0164f352a473869d64
//    val html = getHtml("html\\Java-Scala-Clojure-JavaScript-expert_~0164f352a473869d64.html")
//    //
//    //    val lw = htmlParser.parseWorkSearchResult(html)
//    //    //
//    //      assert(lw.works.size == 10)
//
//  }
//  "Play-Framework-Developer-with-Ajax-jQuery-experience_~01e85edc5fec21632f" in { //https://www.odesk.com/users/Play-Framework-Developer-with-Ajax-jQuery-experience_~01e85edc5fec21632f
//    val html = getHtml("html\\Play-Framework-Developer-with-Ajax-jQuery-experience_~01e85edc5fec21632f.html")
//    //
//    //    val lw = htmlParser.parseWorkSearchResult(html)
//    //    //
//    //      assert(lw.works.size == 10)
//
//  }
//  "Scala-Java-Groovy-developer-Full-Stack-Engineer-Consultant_~01de607007b5965edf" in { //https://www.odesk.com/users/Scala-Java-Groovy-developer-Full-Stack-Engineer-Consultant_~01de607007b5965edf
//    val html = getHtml("html\\Scala-Java-Groovy-developer-Full-Stack-Engineer-Consultant_~01de607007b5965edf.html")
//    //
//    //    val lw = htmlParser.parseWorkSearchResult(html)
//    //    //
//    //      assert(lw.works.size == 10)
//
//  }
//


  }
