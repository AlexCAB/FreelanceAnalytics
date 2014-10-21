package excavators.odesk.parsers

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import excavators.odesk.structures._

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
  val applicantsParamP = List("oH2 oHToggle","oRight applicantListBidRangeBlock")
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
  val sepW = "[|]"
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
  val applicantListId = "applicantList"
  val applicantListBidRangeId = "applicantListBidRange"
  //Helpers
  private val oDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
  private val oShortDateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
  private val oFullDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
  private implicit class ElsHelper(es:Elements){
    def toListOpt:List[Option[Element]] = (for(i <- 0 until es.size())yield es.get(i)).toList.map(Some(_))
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
    def getChildList:List[Option[Element]] = oe match{
      case Some(e) => {
         val es = e.children()
        (for(i <- 0 until es.size()) yield Some(es.get(i))).toList}
      case None => List()}
    def getElemsByClassPath(pl:List[String]):List[Option[Element]] = oe match{
      case Some(e) => {
        val r = getElemsRec(pl, e, (e,n) => {
          val c = Some(e).getChildList.filter{
            case Some(e) => {e.className() == n}
            case _ => false}
          c.map(_.get)})
        r.map(Some(_))}
      case None => List()}
    def getElemsByTagPath(pl:List[String]):List[Option[Element]] = oe match{
      case Some(e) => getElemsRec(pl, e, (e,n) => e.children().select(n).toList).map(Some(_))
      case None => List()}
    def getElemTextByClassPath(p:List[String]):Option[String] = {
      oe.getElemsByClassPath(p).headOption.flatMap(_.map(_.text()))}
    def getTableMap:Map[String,String] = oe match {
      case Some(e) => {
        e.children().select(tableTeg).first().children().select(trTeg).toList match{
          case le if(le.nonEmpty) => le.map(e => (e.select(thTeg).text(), e.children().select(tdTeg).text())).toMap
          case _ => Map()}}
      case None => Map()}
    def getElementByIdOpt(id:String):Option[Element] = oe.flatMap(_.getElementById(id) match{
      case e:Element => Some(e)
      case _ => None})
    def getElemsByTeg(t:String):List[Option[Element]] = oe match {
      case Some(e) => e.children().select(t).toListOpt
      case None => List()}
    def getElemsByClass(cn:String):List[Option[Element]] = oe match {
      case Some(e) => e.children().toList.filter(e => {e.className() == cn}).map(Some(_))
      case None => List()}
    def getAllElemsByClass(cn:String):List[Option[Element]] = oe match {
      case Some(e) => e.getAllElements.toList.filter(e => {e.className() == cn}).map(Some(_))
      case None => List()}
    def findElementByIdOpt(id:String):Option[Element] = oe.flatMap(e => {
      e.getAllElements.toList.find(e => {e.id() == id})})
   def getText:Option[String] = oe.flatMap(_.text() match{case "" => None; case s => Some(s)})
    def getAttr(n:String):Option[String] = oe.flatMap(_.attr(n) match{case "" => None; case s => Some(s)})}
  private implicit class StringHelper(os:Option[String]){
    private def getNum(s:String):String = s.filter(c => List('0','1','2','3','4','5','6','7','8','9','.').contains(c))
    def pSplit:List[String] = os match{
      case Some(s) => s.replaceAll("\\s+"," ").split("[ ]+").filter(_ != "").toList
      case None => List()}
    def sepSplit(sp:String):List[String] = os.pSplit.mkString(" ").split(sp).toList
    def parseTimeAgo(cd:Date):Option[Date] = { //Return past date
      def pi(t:String):Option[Long] = try{Some(t.toInt)}catch{case _:Exception => None}
      def nd(om:Option[Long]):Option[Date] = om.flatMap(m => Some(new Date(cd.getTime - (1000 * 60 * m))))
      os.pSplit match{
        case ws if(ws.contains("minute")) => nd(Some(1))
        case n :: f :: _ if(f == "minutes") => nd(pi(n))
        case ws if(ws.contains("hour")) => nd(Some(60))
        case n :: f :: _ if(f == "hours") => nd(pi(n).map(_ * 60))
        case n :: f :: _ if(f == "day") => nd(pi(n).map(_ * 60 * 24))
        case n :: f :: _ if(f == "days") => nd(pi(n).map(_ * 60 * 24))
        case n :: f :: _ if(f == "week") => nd(pi(n).map(_ * 60 * 24 * 7))
        case n :: f :: _ if(f == "weeks") => nd(pi(n).map(_ * 60 * 24 * 7))
        case ws if(ws.contains("month")) => nd(Some(60 * 24 * 30))
        case n :: f :: _ if(f == "months") => nd(pi(n).map(_ * 60 * 24 * 30))
        case s :: _ if(s.contains('/')) => try{Some(new Date(oFullDateFormat.parse(s).getTime))}catch{case _:Exception => None}
        case _ => None}}
    def parseDate:Option[Date] = os match{
      case s:Some[String] => try{Some(new Date(oDateFormat.parse(s.pSplit.mkString(" ")).getTime))}catch{case _:Exception => None}
      case None => None}
    def parseShortDate:Option[Date] = os match{
      case s:Some[String] => try{
        Some(new Date(oShortDateFormat.parse(s.pSplit.mkString(" ")).getTime))}catch{case e:Exception => None}
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
  private implicit class ElemListHelper(el:List[Option[Element]]){
    def getHead:Option[Element] = el.headOption.flatMap(e => e)}
  //Methods
  def parseWorkSearchResult(html:String):ParsedSearchResults = {
    //Parsing
    val d = Jsoup.parse(html)
    //Extract jobs data
    val b = Some(d.body)//.getAllElements.toListOpt
    val rl = b.getAllElemsByClass(jobClassName).map(j => {
      //Ger values
      val ds = j.getChildList.flatMap{
        case e:Some[Element] => e.get.className() match{
          case `titleClassName` => List(titleClassName -> e.getElemsByClass(linkClassName).getHead.getAttr(hrefAttr))
          case `hiresNumClassName` => {
            val as = e.getText.pSplit
            val r = try{Some(as(3).toInt)}catch{case _:Exception => None}
            List(hiresNumClassName -> r)}
          case `infoClassName` => {
            e.getElemsByClass(skillsClassName).getHead match{
              case e:Some[Element] => e.getElemsByTeg(aTeg).map(_.getAttr(skillAttr))  match{
                case l:List[Option[String]]=> List(infoClassName -> l.filter(_.nonEmpty).map(_.get))
                case _ => List()}
              case None => List()}}
          case _ => List()}
        case None => List()}
      //Build FoundWork structure
      ds match{
        case List((`titleClassName`,Some(u:String))) =>
          FoundWork(u,List(),None)
        case List((`titleClassName`,Some(u:String)), (`infoClassName`, s:List[String])) =>
          FoundWork(u,s,None)
        case List((`titleClassName`,Some(u:String)),(`hiresNumClassName`, r:Option[Int])) =>
          FoundWork(u,List(),r)
        case List((`titleClassName`,Some(u:String)),(`hiresNumClassName`, r:Option[Int]), (`infoClassName`, s:List[String])) =>
          FoundWork(u,s,r)
        case _ =>
          FoundWork("",List(),None)}})
    //Extract extra data
    val (nf,nu) = b.getAllElemsByClass(mainClassName).getHead match{
      case m:Some[Element] => {
        val f = m.getElemsByClassPath(numFindPath).map(_.getElemsByTeg(h4Tag).getHead.getText.pSplit.headOption.parseInt).headOption.flatMap(e => e) //Number of found
        val n = m.getElemsByClassPath(nextFindPath).find(_.getText == Some(nextText)).flatMap(_.getAttr(hrefAttr))
        (f,n)}
      case None => (None,None)}
    //Return result
    ParsedSearchResults(rl,nf,nu)}
  def parseJob(html:String):Option[ParsedJob] = {
    //Parsing
    val b = Some(Jsoup.parse(html).body())
    //Get parts
    b.getAllElemsByClass(mainCN).headOption.map(m => { //Job data
      //General preparing
      val cd = new Date
     // val wd = m.getElemsByClassPath(workDataP).getHead
      //Extract job data
      val j = {
        //Prepare job data
        val jpt = m.getElemTextByClassPath(jobPaymentP).pSplit match{ //Job payment type
          case lw if(lw.contains(hourlyW)) => Payment.Hourly
          case lw if(lw.contains(fixedW))=> Payment.Budget
          case _ => Payment.Unknown}
        val eal = m.getElemTextByClassPath(employmentAndLengthP).pSplit
          //Build job data
          Job(
          createDate = cd,
          postDate = m.getElemsByClassPath(postedP).getHead match{
            case e:Some[Element] => e.getElementByIdOpt(postedId).map(_.text()).parseTimeAgo(cd)
            case None => None},
          deadline = Some(m.getElemTextByClassPath(deadlineP).pSplit.reverse.take(3).reverse.mkString(" ")).parseDate,
          jobTitle = m.getElemTextByClassPath(titleP),
          jobType = m.getElemTextByClassPath(jobTypeP),
          jobPaymentType = jpt,
          jobPrice = m.getElemTextByClassPath(jobPriceP).parseDouble,
          jobEmployment = jpt match{
            case Payment.Hourly => eal match {
              case ws if(ws.contains(neededW)) => Employment.AsNeeded
              case ws if(ws.contains(fullW)) => Employment.Full
              case ws if(ws.contains(partW)) => Employment.Part
              case _ => Employment.Unknown}
            case _ => Employment.Unknown},
          jobLength = jpt match{
            case Payment.Hourly =>  eal match {
              case sa if(sa.size >= 2) => Some(sa.drop(2).mkString(" "))
              case _ => None}
            case _ => None},
          jobRequiredLevel = m.getElemTextByClassPath(jobSkillP).pSplit match{
            case ws if(ws.contains(entryW)) => SkillLevel.Entry
            case ws if(ws.contains(intermediateW)) => SkillLevel.Intermediate
            case ws if(ws.contains(expertW)) => SkillLevel.Expert
            case _ => SkillLevel.Unknown},
          jobQualifications = m.findElementByIdOpt(jobQualificationsId).getTableMap,
          jobDescription = m.getElementByIdOpt(jobDescriptionId) match{
            case Some(e) if(e.children().size() != 0) => Some(e.toString)
            case _ => None})}
      //Extract changes data
      val (jc,cc) = {
        //Prepare changes data
        val ja = ! b.getAllElemsByClass(errorCN).exists(e => { //Job available?
          val ws = e.getText.pSplit
          ws.contains(availableW) &&  ws.contains(noW)})
        val wda = m.findElementByIdOpt(jobActivityId) match{case e:Some[Element] => e.getTableMap; case _ => Map[String, String]()}
        val ap = wda.findByKeyPart(applicantsW)
        val in = wda.findByKeyPart(interviewingW)
        val hr = wda.findByKeyPart(hiresW)
        val app = m.getElementByIdOpt(jobApplicantId).getElementByIdOpt(applicantListId).getElementByIdOpt(applicantListBidRangeId) match{
          case e:Some[Element] => {
            e.getElemsByTeg(strongTeg).getHead.getText.sepSplit("[|]").toList.map(e => {
              Some(e).pSplit match{
                case f :: s :: _ => (f,s)
                case _ =>  ("","")}}).toMap}
          case None => Map[String,String]()}
        val (ci,cr,cs) = b.getAllElemsByClass(sideCN).getHead match{ //Client data (client info, client rating, client specialty)
          case Some(e) => {
            val ae = e.getAllElements
            (ae.toList.find(_.className() == companyCN),
             ae.toList.find(_.className() == feedbackCN),
             ae.toList.find(_.className() == detailsCN))}
          case None => (None,None,None)}
        val crs = cr.getElemsByTagPath(List(divTag,spanTag)).getHead.getText.pSplit //Client rating string
        val csm = cs match{ //Client specialty map
          case e:Some[Element] => e.getChildList.map(e => {
            val wa = e.getText.pSplit
            clientDetailsWM.find{case (_,lw) => wa.exists(w => lw.contains(w))} match{
              case Some((k,_)) => (k, wa.toList)
              case None => ("",List())}}).toMap
          case None => Map[String,List[String]]()}
        //Build changes data
        (JobChanges(
          createDate = cd,
          jobAvailable = (if(ja) JobAvailable.Yes else JobAvailable.No),
          lastViewed = wda.findByKeyPart(lastW) match{
            case Some(s) => Some(s).parseTimeAgo(cd)
            case _ => None},
          nApplicants = ap.pSplit.headOption.parseInt,
          applicantsAvg = ap.pSplit.lastOption.parseDouble,
          rateMin = app.findByKeyPart(lowW).parseDouble,
          rateAvg = app.findByKeyPart(avgW).parseDouble,
          rateMax = app.findByKeyPart(highW).parseDouble,
          nInterviewing = in.pSplit.headOption.parseInt,
          interviewingAvg = in.pSplit match{
            case _ :: s :: _ => Some(s).parseDouble
            case _ => None},
          nHires = hr.pSplit.headOption.parseInt),
        ClientChanges(
          createDate = cd,
          name = ci.map(_.children().select(h4Tag).text()),
          logoUrl = ci.flatMap(_.children().select(imgTag).attr(srcAttr) match{case "" => None; case s => Some(s)}),
          url = ci.map(_.children().select(divTag).select(aTeg).text()),
          description = ci.getText,
          paymentMethod = cr match{
            case e:Some[Element] => e.getText.pSplit match{
              case lw if(lw.contains(notW) && lw.contains(verifiedW)) =>  PaymentMethod.No
              case _ => PaymentMethod.Verified}
            case None => PaymentMethod.Unknown},
          rating = crs.headOption.parseDouble,
          nReviews = crs.drop(1).headOption.parseInt,
          location = csm.getOrElse("l",List()).dropRight(2) match{
            case Nil => None
            case l => Some(l.mkString(" "))},
          time = csm.getOrElse("l",List()).takeRight(2) match{
            case Nil => None
            case l => Some(l.mkString(" "))},
          nJobs = csm.getOrElse("h",List()).headOption.parseInt,
          hireRate = csm.getOrElse("h",List()).drop(3).headOption.parseInt,
          nOpenJobs = csm.getOrElse("h",List()).drop(6).headOption.parseInt,
          totalSpend = Some(csm.getOrElse("s",List()).take(2).mkString("")).parseDouble,
          nHires = csm.getOrElse("s",List()).reverse.drop(3).headOption.parseInt,
          nActive = csm.getOrElse("s",List()).reverse.drop(1).headOption.parseInt,
          avgRate = csm.getOrElse("a",List()).headOption.parseDouble,
          hours = csm.getOrElse("a",List()).reverse.drop(1).headOption.parseInt,
          registrationDate = Some(csm.getOrElse("m",List()).drop(2).mkString(" ")).parseDate))}
      //Extract applicants data
      val al = {
        //Prepare applicants data
        val el = m.getElementByIdOpt(jobApplicantId).getElemsByTagPath(List(sectionTeg, tableTeg, tbodyTeg, trTeg))
        //Build applicants data
        el.map(e => {
          val cl = e.getElemsByTeg(tdTeg)
          JobApplicant(
            createDate = cd,
            upDate = cl match{
              case _ :: se :: _ => se.getText.parseTimeAgo(cd)
              case _ => None},
            name = cl match{case fe :: _ => fe.getText; case _ => None},
            initiatedBy = cl match{
              case _ :: _ :: te :: _ => te.getText.pSplit match{
                case lw if(lw.contains(clientW)) => InitiatedBy.Client
                case lw if(lw.contains(freelancerW)) => InitiatedBy.Freelancer
                case _ => InitiatedBy.Unknown}
              case _ => InitiatedBy.Unknown},
            url = cl match{
              case fe :: _ => fe.getElemsByTeg(aTeg).getHead.getAttr(hrefAttr)
              case _ => None})})}
      //Extract hired data
      val hl = {
        //Preparing hired data
        val t =  m.findElementByIdOpt(jobActivityId).getElemsByTagPath(List(tableTeg, tbodyTeg, trTeg))
        val al = t.find(_.getElemsByTeg(thTeg).getHead.getText.pSplit.contains(hiredW)).flatMap(e => e).getElemsByTagPath(List(tdTeg, ulTeg, liTeg, aTeg))
        //Build hired data
        al.map(e => {
          JobHired(
            createDate = cd,
            name = e.getText,
            freelancerUrl = e.getAttr(hrefAttr))})}
      //Extract works data
      val wl = {
        //Preparing works data
        val es = m.getElementByIdOpt("jobHistorySection").getElemsByTeg(articleTeg)
        //Build works data
        es.map(e => {
          val t = e.getElemsByClassPath(List("col col4of5","oRowTitle")).getHead
          val d = e.getElemsByClassPath(List("col col4of5")).getHead
          val f = d.getElemsByTeg(divTag).getHead
          val wd = e.getElemsByClassPath(List("col col1of5 txtRight oSupportInfo")).getHead.getElemsByTeg(divTag)
          val ds = wd match{case e :: _ => e.getText.sepSplit("-"); case _ => List()}
          val p = (wd match{case _ :: e :: _ => e.getText; case _ => None})
          val b = wd match{case _ :: _ :: e :: _ => e.getText; case _ => None}
          val pm = p.pSplit match{
            case lw if(lw.contains("@")) => Payment.Hourly
            case lw if(lw.contains("Fixed")) => Payment.Budget
            case _ => Payment.Unknown}
          ClientWork(
            createDate = cd,
            oUrl = t.getElemsByTeg(aTeg).getHead.getAttr(hrefAttr),
            title = t.getText,
            inProgress = d match{
              case e:Some[Element] if(e.getElemsByTeg(iTag).nonEmpty) => JobState.InProcess
              case e:Some[Element] if(e.getElemsByTeg(pTag).nonEmpty) => JobState.End
              case _ => JobState.InProcess},
            startDate = ds match{
              case s :: _ => Some(s.split(" ").take(2).mkString(" ")).parseShortDate
              case _ => None},
            endDate = ds match{
              case _ :: s :: _ if(! s.split(" ").contains("Present")) => Some(s).parseShortDate
              case _ => None},
            paymentType = pm,
            billed = pm match{
              case Payment.Hourly => b.pSplit.drop(1).headOption.parseDouble
              case Payment.Budget => p.pSplit.drop(2).headOption.parseDouble
              case _ => None},
            hours =  pm match{
              case Payment.Hourly => p.sepSplit("@").headOption.parseInt
              case _ => None},
            rate = pm match{
              case Payment.Hourly => p.sepSplit("@").lastOption.parseDouble
              case _ => None},
            freelancerFeedbackText = d.getElemsByTeg(pTag).getHead.getText,
            freelancerFeedback = d.getElemsByTagPath(List(pTag,spanTag)).getHead.getAttr(dataContentTeg).parseDouble,
            freelancerName = f.getElemsByTeg(aTeg).getHead.getText match{
              case Some(s) => Some(s)
              case None => f.getElemsByTeg(strongTeg).getHead.getText},
            freelancerUrl =f.getElemsByTeg(aTeg).getHead.getAttr(hrefAttr),
            clientFeedback = f.getElemsByTeg(spanTag).getHead.getAttr(dataContentTeg).parseDouble)})}
      //Return result
      ParsedJob(
        job = j,
        jobChanges = jc,
        clientChanges = cc,
        applicants = al,
        hires = hl,
        clientWorks = wl)})}}




















































