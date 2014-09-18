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

class HTMLParsersTest extends FlatSpec with Matchers {
  "HTMLParsers" should "parseWorkSearchResult" in {
    val htmlParser = new HTMLParsers(new TestLogger)
    val uri = getClass.getResource("html\\SearchResult.html").toURI
    val html = io.Source.fromFile(uri).mkString
    //
    val lw = htmlParser.parseWorkSearchResult(html)
    //
    assert(lw.size == 10)
    assert(lw(0) == FoundWork(
      url = "/jobs/IOS-and-Android-Tour-App_%7E015374e4dd7938042f",
      skills = List("android-app-development", "apple-xcode", "core-java", "eclipse", "iphone-app-development", "jquery-mobile", "objective-c", "phonegap"),
      nFreelancers = None))
    assert(lw(1) == FoundWork(
      url = "/jobs/09162014_173364_Translation_French_350_%7E012efa5e628d98922f",
      skills = List(),
      nFreelancers = None))
    assert(lw(9) == FoundWork(
      url = "/jobs/Write-articles-about-Financial-planning_%7E012df1b84c43dca7d8",
      skills = List("article-writing", "blog-writing", "financial-management", "insurance-consulting"),
      nFreelancers = Some(3)))}
  "HTMLParsers" should "parseJob" in {
    val htmlParser = new HTMLParsers(new TestLogger)
    val uri = getClass.getResource("html\\Job.html").toURI //https://www.odesk.com/jobs/Native-British-English-Copywriter-for-Mechanical-Engineering-Website_~01364df16c276d7604
    val html = io.Source.fromFile(uri).mkString
    val df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.ENGLISH)
    //
    val tr = htmlParser.parseJob(html).get
    //
    val pd = Calendar.getInstance()
    pd.setTime(new Date)
    pd.add(Calendar.HOUR, -1)
    assert(tr.oUrl == "/jobs/Native-British-English-Copywriter-for-Mechanical-Engineering-Website_%7E01364df16c276d7604")
    assert(tr.foundBy == FoundBy.Search)
    assert(tr.postDate == Some(pd.getTime))
    assert(tr.daeDate == Some(df.parse("2014.Nov.15 00:00:00")))


    assert(tr.deleteDate == Option[Date])
    assert(tr.nextCheckDate == Option[Date])
    assert(tr.jobTitle == Option[String])
    assert(tr.jobTypeTags == List[String])
    assert(tr.jobPaymentType == Payment)
    assert(tr.jobPrice == Option[Double])
    assert(tr.jobEmployment == Employment)
    assert(tr.jobLength == Option[Int])
    assert(tr.jobRequiredLevel == SkillLevel)

//    assert(tr.jobSkills == List[String])
//    assert(tr.jobDescription == Option[String])
//    assert(tr.changes == List[JobChanges])






  }






}

