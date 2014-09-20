package excavators.odesk
import scala.collection.mutable.{Map => MutMap, Set => MutSet, ListBuffer => MutList}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.{Locale, Date}
import java.text.SimpleDateFormat
import java.awt.Image

/**
 * Set of HTML parsers for oDesk site
 * Created by CAB on 16.09.14.
 */

class HTMLParsers{
  //Parameters
  val jobClassName = "oMed oJobTile jsSimilarTile"
  val titleClassName = "oRowTitle oH3"
  val linkClassName = "oVisitedLink"
  val infoClassName = "oSupportInfo"
  val skillsClassName = "jsSkills oSkills inline"
  val skillClassName = "oSkill oTagSmall oTag"
  val hiresNumClassName = "oFormMsg oFormInfo oHiresNumber p"
  val mainClassName = "oMain"
  val numFindPath = List("oListLite jsSearchResults","oBreadcrumbBar","oLeft")
  val nextFindPath = List("oListLite jsSearchResults","oBreadcrumbBar","oPagination txtRight","oPager")
  val nextText = "Next"
  val hrefAttr = "href"
  val srcAttr = "src"
  val skillAttr = "data-skill"
  val h4Tag = "h4"
  val sectionTeg = "section"
  val divTag = "div"
  val aTeg = "a"
  val tbodyTeg = "tbody"
  val mainCN = "oMain"
  val sideCN = "oSide"
  val titleP = List("oJobHeader","oH1Huge np")
  val jobTypeP = List("oJobHeader","p","oTag oSkill")
  val jobPaymentP = List("oJobHeader","oLeft oJobHeaderBlock","oJobHeaderContent","oH4")
  val hourlyW = "Hourly"
  val fixedW = "Fixed"
  val jobPriceP = List("oJobHeader","oRight oJobHeaderBlock","oJobHeaderContent","oH4")
  val employmentAndLengthP = List("oJobHeader","oLeft oJobHeaderBlock","oJobHeaderContent","oNull")
  val neededW = "Needed"
  val fullW = "Full"
  val partW  = "Part"
  val jobSkillP = List("oJobHeader","oRight oJobHeaderBlock","oJobHeaderContent","oH4")
  val entryW = "Entry"
  val intermediateW = "Intermediate"
  val expertW = "Expert"
  val jobDescriptionId = "jobDescriptionSection"
  val postedP = List("oJobHeader","p","oTxtSmall oJobHeaderCtime","oNull")
  val postedId = "jobsJobsHeaderCtime"
  val deadlineP = List("oJobHeader","oLeft oJobHeaderBlock","oJobHeaderContent","oNull")
  val errorCN = "oMsg oMsgError"
  val availableW = "available"
  val noW = "no"
  val workDataP = List("cols")
  val jobActivityId = "jobActivitySection"
  val tableTeg = "table"
  val trTeg = "tr"
  val thTeg = "th"
  val tdTeg = "td"
  val iTag = "i"
  val jobQualificationsId = "jobQualificationsSection"
  val applicantsParamP = List("jobApplicantListSection","oH2 oHToggle","oRight applicantListBidRangeBlock")
  val applicantsW = "Applicants:"
  val interviewingW = "Interviewing:"
  val strongTeg = "strong"
  val imgTag = "img"
  val spanTag = "span"
  val ulTeg = "ul"
  val articleTeg = "article"
  val liTeg = "li"
  val dataContentTeg = "data-content"
  val pTag = "p"
  val sepW = "|"
  val lastW = "Last"
  val lowW = "Low"
  val avgW = "Avg"
  val highW = "High"
  val hiresW = "Hires:"
  val clientDetailsWM = Map(
    "l" -> List("AM","PM"),
    "h" -> List("Jobs"),
    "s" -> List("Spent"),
    "a" -> List("Avg"),
    "m" -> List("Member"))
  val companyCN = "oTxtSmall oNull p oJobsAboutBuyerCompany"
  val feedbackCN = "oTxtSmall oNull p oJobsAboutBuyerFeedback"
  val detailsCN = "oJobsAboutBuyerDetails"
  val notW = "Not"
  val verifiedW = "Verified"
  val hiredW = "Hired"
  val jobApplicantId = "jobApplicantListSection"
  val clientW = "Client"
  val freelancerW = "Freelancer"


  //...Далее исправить сплиты, убрать сомы

  //Helpers
  private val oDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
  private val oShortDateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
  private implicit class ElsHelper(es:Elements){
    def toList:List[Element] = (for(i <- 0 until es.size())yield es.get(i)).toList}
  private implicit class ElHelper(oe:Option[Element]){
    private def getElemsRec(pl:List[String], e:Element, fl:(Element,String) => List[Element]):List[Element] = {
      pl match {
        case Nil => List()
        case ph :: pt => {
          val el = fl(e,ph)
          pt match{
            case Nil => el
            case _ => {
              def l(el:List[Element]):List[Element] = el match{
                case Nil => List()
                case eh :: et => {
                  getElemsRec(pt,eh,fl) match{
                    case Nil => l(et)
                    case lf => lf}}}
              l(el)}}}}}
    def getChildList:List[Element] = oe match{
      case Some(e) => {
         val es = e.children()
        (for(i <- 0 until es.size())yield es.get(i)).toList}
      case None => List()}
    def getElemsByClassPath(pl:List[String]):List[Element] = oe match{
      case Some(e) => getElemsRec(pl,e, (e,n) => Some(e).getChildList.filter(_.className() == n))
      case None => List()}
    def getElemsByTagPath(pl:List[String]):List[Element] = oe match{
      case Some(e) => getElemsRec(pl,e, (e,n) => e.children().select(n).toList)
      case None => List()}
    def getElemTextByClassPath(p:List[String]):Option[String] = oe.getElemsByClassPath(p).headOption.map(_.text())
    def getTableMap:Map[String,String] = oe match {
      case Some(e) => {
        e.children().select(tableTeg).first().children().select(trTeg).toList match{
          case le if(le.nonEmpty) => le.map(e => (e.select(thTeg).text(), e.children().select(tdTeg).text())).toMap
          case _ => Map()}}
      case None => Map()}
    def getElementByIdOpt(id:String):Option[Element] = oe.flatMap(_.getElementById(id) match{
      case e:Element => Some(e)
      case _ => None})
    def getElemsByTeg(t:String):List[Element] = oe match {
      case Some(e) => e.children().select(t).toList
      case None => List()}
    def getText:Option[String] = oe.flatMap(_.text() match{case "" => None; case s => Some(s)})
    def getAttr(n:String):Option[String] = oe.flatMap(_.attr(n) match{case "" => None; case s => Some(s)})}
  private implicit class StringHelper(os:Option[String]){
    private def getNum(s:String):String = s.filter(c => List('0','1','2','3','4','5','6','7','8','9','.').contains(c))
    def pSplit:List[String] = os match{
      case Some(s) => s.replaceAll("\\s+"," ").split("[ ]+").filter(_ != "").toList
      case None => List()}
    def parseTimeAgo:Option[Int] = { //Return in min
      def pi(t:String):Option[Int] = try{Some(t.toInt)}catch{case _:Exception => None}
      os.pSplit match{
        case ws if(ws.contains("minute")) => Some(1)
        case n :: f :: _ if(f == "minutes") => pi(n)
        case ws if(ws.contains("hour")) => Some(60)
        case n :: f :: _ if(f == "hours") => pi(n).map(_ * 60)
        case n :: f :: _ if(f == "day") => pi(n).map(_ * 60 * 24)
        case _ => None}}
    def parseDate:Option[Date] = os match{
      case s:Some[String] => try{Some(oDateFormat.parse(s.pSplit.mkString(" ")))}catch{case _:Exception => None}
      case None => None}
    def parseShortDate:Option[Date] = os match{
      case s:Some[String] => try{
        Some(oShortDateFormat.parse(s.pSplit.mkString(" ")))}catch{case e:Exception => None}
      case None => None}
    def parseInt:Option[Int] = os match{
      case Some(s) => try{Some(getNum(s).toInt)}catch{case _:Exception => None}
      case None => None}
    def parseDouble:Option[Double] = os match{
      case Some(s) => try{Some(getNum(s).toDouble)}catch{case _:Exception => None}
      case None => None}}
  private implicit class MapHelper(m:Map[String,String]){
    def findByKeyPart(kp:String):Option[String] = {
      m.find{case (k,_) => Some(k).pSplit.contains(kp)}.map{case (_,v) => v}}}
  //Methods
  def parseWorkSearchResult(html:String):ParsedSearchResults = {
    //Parsing
    val d = Jsoup.parse(html)
    //Extract jobs data
    val ael = d.body.getAllElements.toList
    val rl = ael.filter(e => {e.className() == jobClassName}).map(j => {
      //Ger values
      val ds = Some(j).getChildList.flatMap(e => e.className() match {
        case `titleClassName` => {
          Some(e).getChildList.find(_.className() == linkClassName).map(e => e.attr(hrefAttr)) match{
            case Some("") | None => List()
            case Some(u) => List(titleClassName -> u)}}
        case `hiresNumClassName` => {
          val as = e.text().split(" ")
          val r = try{Some(as(3).toInt)}catch{case _:Exception => None}
          List(hiresNumClassName -> r)}
        case `infoClassName` => {
          Some(e).getChildList.find(_.className() == skillsClassName).map(se => {
            Some(se).getChildList.filter(_.className() == skillClassName).map(e => e.attr(skillAttr))}) match{
              case Some(List()) | None => List()
              case Some(s) => List(infoClassName -> s)}}
        case _ => List()})
      //Build FoundWork structure
      ds match{
        case List((`titleClassName`,u:String)) =>
          FoundWork(u,List(),None)
        case List((`titleClassName`,u:String), (`infoClassName`, s:List[String])) =>
          FoundWork(u,s,None)
        case List((`titleClassName`,u:String),(`hiresNumClassName`, r:Option[Int])) =>
          FoundWork(u,List(),r)
        case List((`titleClassName`,u:String),(`hiresNumClassName`, r:Option[Int]), (`infoClassName`, s:List[String])) =>
          FoundWork(u,s,r)
        case _ =>
          FoundWork("",List(),None)}})
    //Extract extra data
    val (nf,nu) = ael.find(_.className() == mainClassName) match{
      case m:Some[Element] => {
        val f = m.getElemsByClassPath(numFindPath).map(e => {
          try{
            Some(e.children().select(h4Tag).first().text().split(" ")(0).filter(_ != ',').toInt)}
          catch{
            case _:Exception => None}})
        val n = m.getElemsByClassPath(nextFindPath).find(_.text == nextText).map(_.attr(hrefAttr))
        ((if(f.isEmpty) None else f.head),n)}
      case None => (None,None)}
    //Return result
    ParsedSearchResults(rl,nf,nu)}
  def parseJob(html:String):Option[ParsedJob] = {
    //Parsing
    val d = Jsoup.parse(html).body().getAllElements.toList
    //Get parts
    d.find(_.className() == mainCN).map(fm => { //Job data
      //General preparing
      val m = Some(fm)
      val cd = new Date
      val wd = m.getElemsByClassPath(workDataP).headOption
      //Extract job data
      val j = {
        //Prepare job data
        val ja = d.filter(_.className() == errorCN).exists(e =>{ //Job available?
          val ws = e.text().split(" ")
          ws.contains(availableW) &&  ws.contains(noW)})
        val jpt = m.getElemTextByClassPath(jobPaymentP) match{ //Job payment type
          case Some(s) if(s.split(" ").contains(hourlyW)) => Payment.Hourly
          case Some(s) if(s.split(" ").contains(fixedW))=> Payment.Budget
          case _ => Payment.Unknown}
        val eal = m.getElemTextByClassPath(employmentAndLengthP).map(_.split(" "))
          //Build job data
          Job(
          date = cd,
          postDate = m.getElemsByClassPath(postedP).headOption match{
            case e:Some[Element] => e.getElementByIdOpt(postedId).map(_.text()).parseTimeAgo match{
              case Some(x) => Some(new Date(cd.getTime - (x * 60 * 1000)))
              case None => None}
            case None => None},
          deadline = m.getElemTextByClassPath(deadlineP).map(s => s.split(" ").reverse.take(3).reverse.mkString(" ")).parseDate,
          daeDate = (if(ja) Some(cd) else None),
          jobTitle = m.getElemTextByClassPath(titleP),
          jobType = m.getElemTextByClassPath(jobTypeP),
          jobPaymentType = jpt,
          jobPrice = m.getElemTextByClassPath(jobPriceP).parseDouble,
          jobEmployment = jpt match{
            case Payment.Hourly => eal match {
              case Some(sa) if(sa.size >= 2) => sa.take(2) match{
                case ws if(ws.contains(neededW)) => Employment.AsNeeded
                case ws if(ws.contains(fullW)) => Employment.Full
                case ws if(ws.contains(partW)) => Employment.Part
                case b => Employment.Unknown}
              case _ => Employment.Unknown}
            case _ => Employment.Unknown},
          jobLength = jpt match{
            case Payment.Hourly =>  eal match {
              case Some(sa) if(sa.size >= 2) => Some(sa.drop(2).mkString(" "))
              case _ => None}},
          jobRequiredLevel = m.getElemTextByClassPath(jobSkillP).map(_.split(" ")) match{
            case Some(ws) if(ws.contains(entryW)) => SkillLevel.Entry
            case Some(ws) if(ws.contains(intermediateW)) => SkillLevel.Intermediate
            case Some(ws) if(ws.contains(expertW)) => SkillLevel.Expert
            case _ => SkillLevel.Unknown},
          jobQualifications = wd match{
            case e:Some[Element] => e.getElementByIdOpt(jobQualificationsId).getTableMap
            case None => Map()},
          jobDescription = m.getElementByIdOpt(jobDescriptionId) match{
            case Some(e) if(e.children().size() != 0) => Some(e.toString)
            case _ => None})}
      //Extract changes data
      val c = {
        //Prepare changes data
        val wda = wd.getElementByIdOpt(jobActivityId) match{case e:Some[Element] => e.getTableMap; case _ => Map[String, String]()}
        val ap = wda.findByKeyPart(applicantsW)
        val in = wda.findByKeyPart(interviewingW)
        val hr = wda.findByKeyPart(hiresW)
        val app = m.getElemsByClassPath(applicantsParamP).headOption match{
          case Some(e) => e.children().select(strongTeg).text().split(sepW).map(_.split(" ").toList match{
            case f :: s :: _ => (f,s)
            case _ => ("","")}).toMap
          case None => Map[String,String]()}
        val (ci,cr,cs) = d.find(_.className() == sideCN) match{ //Client data (client info, client rating, client specialty)
          case Some(e) => {
            val ae = e.getAllElements
            (ae.toList.find(_.className() == companyCN),
             ae.toList.find(_.className() == feedbackCN),
             ae.toList.find(_.className() == detailsCN))}
          case None => (None,None,None)}
        val crs = cr.map(_.children().select(divTag).select(spanTag).text().split(" ")) //Client rating string
        val csm = cs match{ //Client specialty map
          case Some(e) => e.children().toList.map(e => {
            val wa = e.text().split(" ")
            clientDetailsWM.find{case (_,lw) => wa.exists(w => lw.contains(w))} match{
              case Some((k,_)) => (k, wa.toList)
              case None => ("",List())}}).toMap
          case None => Map[String,List[String]]()}
        //Build changes data
        JobChanges(
          date = cd,
          lastViewed = wda.findByKeyPart(lastW) match{
            case Some(s) => Some(s).parseTimeAgo.map(x => {
              new Date(cd.getTime - (x * 60 * 1000))})
            case _ => None},
          nApplicants = ap.flatMap(_.split(" ").headOption.parseInt),
          applicantsAvg = ap.flatMap(_.split(" ").lastOption.parseDouble),
          rateMin = app.findByKeyPart(lowW).parseDouble,
          rateAvg = app.findByKeyPart(avgW).parseDouble,
          rateMax = app.findByKeyPart(highW).parseDouble,
          interviewing = in.flatMap(_.split(" ").headOption.parseInt),
          interviewingAvg = in.flatMap(_.split(" ").lastOption.parseDouble),
          nHires = hr.flatMap(_.split(" ").headOption.parseInt),
          clientName = ci.map(_.children().select(h4Tag).text()),
          clientLogoUrl = ci.flatMap(_.children().select(imgTag).attr(srcAttr) match{case "" => None; case s => Some(s)}),
          clientUrl = ci.map(_.children().select(divTag).select(aTeg).text()),
          clientDescription = ci.map(_.children().select(divTag).text()),
          clientPaymentMethod = cr match{
            case Some(e) => e.text().split(" ") match{
              case lw if(lw.contains(notW) && lw.contains(verifiedW)) =>  PaymentMethod.No
              case _ => PaymentMethod.Verified}
            case None => PaymentMethod.Unknown},
          clientRating = crs.flatMap(_.headOption.parseDouble),
          clientNReviews = crs.flatMap(_.drop(1).headOption.parseInt),
          clientLocation = csm.getOrElse("l",List()).dropRight(2) match{
            case Nil => None
            case l => Some(l.mkString(" "))},
          clientNJobs = csm.getOrElse("h",List()).headOption.parseInt,
          clientHireRate = csm.getOrElse("h",List()).drop(3).headOption.parseInt,
          clientNOpenJobs = csm.getOrElse("h",List()).drop(6).headOption.parseInt,
          clientTotalSpend = Some(csm.getOrElse("s",List()).take(2).mkString("")).parseDouble,
          clientNHires = csm.getOrElse("s",List()).reverse.drop(3).headOption.parseInt,
          clientNActive = csm.getOrElse("s",List()).reverse.drop(1).headOption.parseInt,
          clientAvgRate = csm.getOrElse("a",List()).headOption.parseDouble,
          clientHours = csm.getOrElse("a",List()).reverse.drop(1).headOption.parseInt,
          clientRegistrationDate = Some(csm.getOrElse("m",List()).drop(2).mkString(" ")).parseDate)}
      //Extract applicants data
      val al = {
        //Prepare applicants data
        val el = m.getElementByIdOpt(jobApplicantId).getElemsByTagPath(List(sectionTeg, tableTeg, tbodyTeg, trTeg))
        //Build applicants data
        el.map(e => {
          val cl = e.children().select(tdTeg).toList
          JobApplicant(
            date = cd,
            upDate = cl match{
              case _ :: se :: _ => Some(se.text()).parseTimeAgo.map(x => {new Date(cd.getTime - (x * 60 * 1000))})
              case _ => None},
            name = cl match{case fe :: _ => Some(fe.text()); case _ => None},
            initiatedBy = cl match{
              case _ :: _ :: te :: _ => te.text().split(" ") match{
                case lw if(lw.contains(clientW)) => InitiatedBy.Client
                case lw if(lw.contains(freelancerW)) => InitiatedBy.Freelancer
                case _ => InitiatedBy.Unknown}
              case _ => InitiatedBy.Unknown},
            url = cl match{
              case fe :: _ => {
                fe.children().select(aTeg).attr(hrefAttr) match{
                  case "" => None
                  case s => Some(s)}}
              case _ => None})})}

      //Extract hired data
      val hl = {
        //Preparing hired data
        val t = wd.getElementByIdOpt(jobActivityId).getElemsByTagPath(List(tableTeg, tbodyTeg, trTeg))
        val al = t.find(_.children().select(thTeg).text().split(" ").contains(hiredW)).getElemsByTagPath(List(tdTeg, ulTeg, liTeg, aTeg))
        //Build hired data
        al.map(e => {
          JobHired(
            date = cd,
            name = e.text() match{case "" => None; case s => Some(s)},
            freelancerUrl = e.attr(hrefAttr) match{case "" => None; case s => Some(s)})})}
      //Extract works data
      val wl = {
        //Preparing works data
        val es = m.getElementByIdOpt("jobHistorySection").getElemsByTeg(articleTeg)
        //Build works data
        es.map(fe => {
          val e = Some(fe)
          val t = e.getElemsByClassPath(List("col col4of5","oRowTitle")).headOption
          val d = e.getElemsByClassPath(List("col col4of5")).headOption
          val f = d.getElemsByTeg(divTag).headOption
          val wd = e.getElemsByClassPath(List("col col1of5 txtRight oSupportInfo")).headOption.getElemsByTeg(divTag)
          val ds = wd match{case e :: _ => e.text().split("-").toList; case _ => List()}
          val p = (wd match{case _ :: e :: _ => Some(e).getText; case _ => None})
          val b = wd match{case _ :: _ :: e :: _ => Some(e).getText; case _ => None}
          val pm = p match{
            case Some(s) if(s.split(" ").contains("@")) => Payment.Hourly
            case Some(s) if(s.split(" ").contains("Fixed")) => Payment.Budget
            case _ => Payment.Unknown}


println(ds)

          ClientWork(
            date = cd,
            oUrl = t.getElemsByTeg(aTeg).headOption.map(_.attr(hrefAttr)) match{
              case Some("") => None
              case os => os},
            title = t.getText,
            inProgress = d match{
              case e:Some[Element] if(e.getElemsByTeg(iTag).nonEmpty) => JobState.InProcess
              case e:Some[Element] if(e.getElemsByTeg(pTag).nonEmpty) => JobState.End
              case _ => JobState.InProcess},
            startDate = ds match{
              case s :: _ => Some(s.split(" ").take(2).mkString(" ")).parseShortDate
              case _ => None},
            endDate = ds match{
              case _ :: s :: _ if(! s.split(" ").contains("Present")) => {println(s); Some(s).parseShortDate}
              case _ => None},
            paymentType = pm,
            billed = pm match{
              case Payment.Hourly => b.flatMap(_.split(" ").drop(1).headOption.parseDouble)
              case Payment.Budget => p.flatMap(_.split(" ").drop(2).headOption.parseDouble)
              case _ => None},
            hours =  pm match{
              case Payment.Hourly => p.flatMap(_.split("@").headOption.parseInt)
              case _ => None},
            rate = pm match{
              case Payment.Hourly => p.flatMap(_.split("@").lastOption.parseDouble)
              case _ => None},
            freelancerFeedbackText = d.getElemsByTeg(pTag).headOption.getText,
            freelancerFeedback = d.getElemsByTagPath(List(pTag,spanTag)).headOption.getAttr(dataContentTeg).parseDouble,
            freelancerName = f.getElemsByTeg(aTeg).headOption.getText match{
              case Some(s) => Some(s)
              case None => f.getElemsByTeg(strongTeg).headOption.getText},
            freelancerUrl =f.getElemsByTeg(aTeg).headOption.getAttr(hrefAttr),
            clientFeedback = f.getElemsByTeg(spanTag).headOption.getAttr(dataContentTeg).parseDouble)})}
      //Return result
      ParsedJob(
        job = j,
        changes = c,
        applicants = al,
        hires = hl,
        clientWorks = wl)})}
  //////////////



}




















































