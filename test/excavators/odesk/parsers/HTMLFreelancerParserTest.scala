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
    assert(tr.changes.overview.get.split("").take(3).mkString(" ") == "Certified java developer.")
    assert(tr.changes.location == Some("Minks,  Belarus"))
    assert(tr.changes.timeZone == Some(1))
    assert(tr.changes.languages == List(FreelancerLanguage("English",Some(2),Some(false))))
    assert(tr.changes.photoUrl == Some("https://odesk-prod-portraits.s3.amazonaws.com/Users:n_ivan:PortraitUrl_100?AWSAccessKeyId=1XVAX3FNQZAFC9GJCFR2&Expires=2147483647&Signature=t18gk1%2FK9qGUgOGdU2fZ%2FRpPESo%3D&1415180037"))
    assert(tr.changes.rate == Some(17.22))
    assert(tr.changes.rentPercent == Some(10))
    assert(tr.changes.rating == Some(5.0))
    assert(tr.changes.allTimeJobs == Some(2))
    assert(tr.changes.allTimeHours == Some(3))
    assert(tr.changes.skills == List("Java","Oracle Java EE","Web Services Development","Play Framework","Agile software developmennt","Automated Testing","Web scraping","XSLT","ORM","database programming"))
    assert(tr.changes.companyId == None)





  }
//  "Creator-Software-Made-order_~01aae5df549caa4620" in { //https://www.odesk.com/users/Creator-Software-Made-order_~01aae5df549caa4620
//    val html = getHtml("html\\Creator-Software-Made-order_~01aae5df549caa4620.html")
//    //
////    val lw = htmlParser.parseWorkSearchResult(html)
////    //
////      assert(lw.works.size == 10)
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
