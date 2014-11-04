package excavators.odesk.parsers

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.Date
import util.structures._

/**
 * Set of HTML parsers for oDesk site
 * Created by CAB on 16.09.14.
 */

class HTMLJobParsers extends ParserHelpers{
  //Parameters
  private val jobClassName = "oMed oJobTile jsSimilarTile"
  private val titleClassName = "oRowTitle oH3"
  private val linkClassName = "oVisitedLink"
  private val infoClassName = "oSupportInfo"
  private val skillsClassName = "jsSkills oSkills inline"
  private val skillClassName = "oSkill oTagSmall oTag"
  private val hiresNumClassName = "oFormMsg oFormInfo oHiresNumber p"
  private val mainClassName = "oMain"
  private val numFindPath = List("oListLite jsSearchResults","oBreadcrumbBar","oLeft")
  private val nextFindPath = List("oListLite jsSearchResults","oBreadcrumbBar","oPagination txtRight","oPager")
  private val nextText = "Next"
  private val skillAttr = "data-skill"
  private val mainCN = "oMain"
  private val sideCN = "oSide"
  private val titleP = List("oJobHeader","oH1Huge np")
  private val jobTypeP = List("oJobHeader","p","oTag oSkill")
  private val hourlyW = "Hourly"
  private val fixedW = "Fixed"
  private val jobPriceP = List("oJobHeader","oRight oJobHeaderBlock","oJobHeaderContent","oH4")
  private val employmentAndLengthP = List("oJobHeader","oLeft oJobHeaderBlock","oJobHeaderContent","oNull")
  private val neededW = "Needed"
  private val fullW = "Full"
  private val partW  = "Part"
  private val jobSkillP = List("oJobHeader","oRight oJobHeaderBlock","oJobHeaderContent","oH4")
  private val entryW = "Entry"
  private val intermediateW = "Intermediate"
  private val expertW = "Expert"
  private val jobDescriptionId = "jobDescriptionSection"
  private val postedP = List("oJobHeader","p","oTxtSmall oJobHeaderCtime","oNull")
  private val postedId = "jobsJobsHeaderCtime"
  private val deadlineP = List("oJobHeader","oLeft oJobHeaderBlock","oJobHeaderContent","oNull")
  private val errorCN = "oMsg oMsgError"
  private val availableW = "available"
  private val noW = "no"
  private val workDataP = List("cols")
  private val jobActivityId = "jobActivitySection"
  private val jobQualificationsId = "jobQualificationsSection"
  private val applicantsParamP = List("oH2 oHToggle","oRight applicantListBidRangeBlock")
  private val applicantsW = "Applicants:"
  private val interviewingW = "Interviewing:"
  private val sepW = "[|]"
  private val lastW = "Last"
  private val lowW = "Low"
  private val avgW = "Avg"
  private val highW = "High"
  private val hiresW = "Hires:"
  private val clientDetailsWM = Map(
    "l" -> List("AM","PM"),
    "h" -> List("Jobs"),
    "s" -> List("Spent"),
    "a" -> List("Avg"),
    "m" -> List("Member"))
  private val companyCN = "oTxtSmall oNull p oJobsAboutBuyerCompany"
  private val feedbackCN = "oTxtSmall oNull p oJobsAboutBuyerFeedback"
  private val detailsCN = "oJobsAboutBuyerDetails"
  private val notW = "Not"
  private val verifiedW = "Verified"
  private val hiredW = "Hired"
  private val jobApplicantId = "jobApplicantListSection"
  private val clientW = "Client"
  private val freelancerW = "Freelancer"
  private val applicantListId = "applicantList"
  private val applicantListBidRangeId = "applicantListBidRange"
  private val newJobHeaderP =  List("oJobHeader","cols oFixedPriceHeader")
  private val jobPaymentP = List("oJobHeader","oLeft oJobHeaderBlock","oJobHeaderContent","oH4")
  private val jobNewPayTypeP = List("col col2of6","oJobHeaderContent","oH4")
  private val newDeadLineP = List("col col2of6","oJobHeaderContent","oNull")
  private val newRequiredLevelP = List("col col3of6","oJobHeaderContent","oH4")
  private val newBudgetP = List("col col1of6","oJobHeaderContent","oH4")
  private val notInUrlChars = List('\'','-','\"','&','_','~','%','/',',',':','!', '(',')','.','\\','+','$','[',']','*','?','`',';')
  //Fields
  val logoImageCoordinates = List(7,7,108,108) //x,y,w,h
  val wornParsingQualityLevel = 0.8
  val errorParsingQualityLevel = 0.5
  val notSaveParsingQualityLevel = 0.2
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
       val (jt,dl,rl,b) = m.getElemsByClassPath(newJobHeaderP).headOption.flatMap(e => e) match{ //Try get "cols oFixedPriceHeader" (new job header)
         case jh:Some[Element] =>{
           val jt = jh.getElemTextByClassPath(jobNewPayTypeP)    //Job type
           val dl = jh.getElemTextByClassPath(newDeadLineP)      //DeadLine
           val rl = jh.getElemTextByClassPath(newRequiredLevelP) //Required level
           val b =  jh.getElemTextByClassPath(newBudgetP)        //Budget
           (jt,dl,rl,b)}
         case None =>{  //Old format
           val jt = m.getElemTextByClassPath(jobPaymentP) //Job type
           val dl = m.getElemTextByClassPath(deadlineP)   //DeadLine
           val rl = None                                  //Required level
           val b = m.getElemTextByClassPath(jobPriceP)    //Budget
           (jt,dl,rl,b)}}
       //Prepare job data
        val jpt = jt.pSplit match{ //Job payment type
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
          deadline = Some(dl.pSplit.reverse.take(3).reverse.mkString(" ")).parseDate,
          jobTitle = m.getElemTextByClassPath(titleP),
          jobType = m.getElemTextByClassPath(jobTypeP),
          jobPaymentType = jpt,
          jobPrice = b.parseDouble,
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
          jobRequiredLevel = (jpt match{case Payment.Hourly => b; case _ => rl}).pSplit match{
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
          jobAvailable = (if(ja) Availability.Yes else Availability.No),
          lastViewed = wda.findByKeyPart(lastW) match{
            case Some(s) => Some(s).parseTimeAgo(cd)
            case _ => None},
          nApplicants = ap.pSplit.headOption.parseInt,
          applicantsAvg = ap.extractAvgIfIs,
          rateMin = app.findByKeyPart(lowW).parseDouble,
          rateAvg = app.findByKeyPart(avgW).parseDouble,
          rateMax = app.findByKeyPart(highW).parseDouble,
          nInterviewing = in.pSplit.headOption.parseInt,
          interviewingAvg = in.extractAvgIfIs,
          nHires = hr.pSplit.headOption.parseInt),
        ClientChanges(
          createDate = cd,
          name = ci.map(_.children().select(h4Tag).text()),
          logoUrl = ci.flatMap(_.children().select(imgTag).attr(srcAttr) match{case "" => None; case s => Some(s)}),
          url = ci.map(_.children().select(divTag).select(aTeg).text()).flatMap(s => if(s == ""){None}else{Some(s)}),
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
              case _ => None})}).filter(f => {f.upDate.nonEmpty || f.initiatedBy != InitiatedBy.Unknown})}
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
        clientWorks = wl)})}
  def compareJobURLAndTitle(url:String, title:String):Option[Boolean] = { //Return: Some(true) if match, Some(false) if no, None if unknown.
    val tws = title.map(c => {if(notInUrlChars.contains(c)) ' ' else c}).split(" ").toSet
    val pws = url.split("/").lastOption.flatMap(_ match{
      case s if s.contains('_') => s.split("_").headOption
      case _ => None})
    pws.map(_.split("-").toSet).flatMap{
      case uws if(uws.nonEmpty && tws.nonEmpty) => Some((uws -- tws).isEmpty)
      case _ => None}}
  def estimateParsingQuality(fj:FoundJobsRow, opj:Option[ParsedJob]):Double = opj match {
    case Some(pj) => {
      var r = 1.0
      if(pj.job.postDate == None){r -= 0.3}
      if(pj.job.deadline == None && pj.job.jobPaymentType == Payment.Budget){r -= 0.05}
      if(pj.job.jobTitle == None){r -= 1.0}
      if(pj.job.jobType == None){r -= 0.2}
      if(pj.job.jobPaymentType == Payment.Unknown){r -= 0.3}
      if(pj.job.jobEmployment == Employment.Unknown && pj.job.jobPaymentType == Payment.Hourly){r -= 0.1}
      if(pj.job.jobPrice == None && pj.job.jobPaymentType == Payment.Budget){r -= 0.3}
      if(pj.job.jobLength == None && pj.job.jobPaymentType == Payment.Hourly){r -= 0.1}
      if(pj.job.jobRequiredLevel == SkillLevel.Unknown && pj.job.jobPaymentType == Payment.Hourly){r -= 0.1}
      if(pj.job.jobDescription == None){r -= 0.3}
      if(pj.jobChanges.nApplicants == None){r -= 0.05}
      if(pj.clientChanges.paymentMethod == PaymentMethod.Unknown){r -= 0.1}
      if(pj.clientChanges.location == None){r -= 0.05}
      if(pj.job.jobTitle.nonEmpty){
        compareJobURLAndTitle(fj.oUrl, pj.job.jobTitle.get) match{
          case None => {r -= 0.3}
          case Some(false) => {r -= 1.0}
          case _ => }}
      if(r < 0.0){r = 0.0}
      r}
    case None => 0.0}
}




















































