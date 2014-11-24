package excavators.odesk.parsers

import java.util.Date
import org.json.{JSONArray, JSONObject}
import org.jsoup.Jsoup
import util.structures._

/**
 * Set of HTML parsers for freelancers profiles
 * Created by CAB on 04.11.2014.
 */

class HTMLFreelancerParser extends ParserHelpers {
  //Parameters
  //Fields
  val photoImageCoordinates = List(7,7,108,108) //x,y,w,h
  val companyLogoImageCoordinates = List(7,7,108,108) //x,y,w,h
  val clientProfileLogoImageCoordinates = List(7,7,108,108) //x,y,w,h
  val wornParsingQualityLevel = 0.8
  val errorParsingQualityLevel = 0.5
  val notSaveParsingQualityLevel = 0.2
  //Methods
  def parseFreelancerProfile(html:String):FreelancerParsedData = {
    //Current date9
    val cd = new Date
    //Parsing of HTML
    val ph = try{Some(Jsoup.parse(html).body())}catch{case _:Exception ⇒ None}
    val om = ph.getAllElemsByClass("oMain").headOption.flatMap(e ⇒ e)
    val os = ph.getAllElemsByClass("oSide").headOption.flatMap(e ⇒ e)
    //Parsing of JSON
    val pt = html.split("odesk.pageConfig.").drop(1).map(s ⇒ {
      val k = s.takeWhile(_ != ' ')
      val v = s.dropWhile(_ != '=').drop(1).dropWhile(_ == ' ')
      (k, v)}).toMap
    val pjo = pt.flatMap{
      case(k,v) if(v != "" && v.take(1) == "{") ⇒ Map(k -> (try{Some(new JSONObject(v))}catch{case _:Exception ⇒ None}))
      case _ ⇒ Map[String,Option[JSONObject]]()}
    val pja = pt.flatMap{
      case(k,v) if(v != "" && v.take(1) == "[") ⇒ Map(k -> (try{Some(new JSONArray(v))}catch{case _:Exception ⇒ None}))
      case _ ⇒ Map[String,Option[JSONArray]]()}
    //FreelancerChanges
    val fcd = {
      //Preparing
      val tl = om.getAllElemsByClass("oMed jsHideWhenEditing").headOption.flatMap(e ⇒ e)
      val (rw,hw,njw) = os.getText.pSplit match {
        case "Work" :: "history" :: r :: h :: _ :: _ :: nj :: _ ⇒ (Some(r).parseDouble, Some(h).parseInt, Some(nj).parseInt)
        case _ ⇒ (None, None, None)}
      val (cu,clu) = {
        val p = List("oMed p","oImg","oAssocAgencyLogoLink txtMiddle oPortraitSmall")
        val s = os.getElemsByTeg("section").find(e ⇒ {val s = e.getText.pSplit.take(2); s.contains("Associated")})
        val a = s.flatMap(e ⇒ e.getElemsByClassPath(p).headOption).flatMap(e ⇒ e)
        (a.getAttr("href"), a.getElemsByTeg("img").headOption.flatMap(_.getAttr("src")))}
      //Building
      FreelancerChanges(
        createDate = cd,
        name =  pjo.getTopString("contractorTitleData","displayName"),
        link = pjo.getTopString("contractorTitleData","link"),
        title = pjo.getTopString("contractorTitleData","titleHtml"),
        profileAccess = pjo.getStringByPath("contractorAvailability",List("data","profileAccess")),
        exposeFullName = pjo.getTopString("contractorTitleData","exposeFullName"),
        role = pt.getFirstWord("userRole"),
        emailVerified = pt.getFirstWord("emailVerified") match{
          case Some("true") ⇒ Verified.Yes
          case Some("false") ⇒ Verified.No
          case _ ⇒ Verified.Unknown},
        videoUrl = pjo.getTopString("contractorOverviewData","videoUrl"),
        isInviteInterviewAllowed = pt.getFirstWord("isInviteInterviewAllowed") match{
          case Some("true") ⇒ Allowed.Yes
          case Some("false") ⇒ Allowed.No
          case _ ⇒ Allowed.Unknown},
        availability =  pjo.getStringByPath("contractorAvailability",List("data","capacity")) match{
          case Some("fullTime") ⇒ FreelancerAvailable.FullTime
          case Some("partTime") ⇒ FreelancerAvailable.PartTime
          case Some("lessThen") ⇒ FreelancerAvailable.LessThen
          case _ ⇒ FreelancerAvailable.Unknown},
        availableAgain = pjo.getStringByPath("contractorAvailability",List("data","availableAgain")),
        responsivenessScore = pjo.getTopString("contractorAvailability","responsivenessScore"),
        overview = pjo.getTopString("contractorOverviewData","overviewHtml"),
        location = tl.getElemTextByClassPath(List("oBd","oRowTitle oTextBoxLiner oTxtMed")),
        timeZone = tl.getElemTextByClassPath(List("oBd","oMute")).pSplit.reverse match {
          case "behind" :: _ :: t :: _ ⇒ Some(t).parseInt.map(_ * -1)
          case "ahead" :: _ :: t :: _ ⇒ Some(t).parseDouble
          case _ ⇒ None},
        languages = os.findElementByIdOpt("jsLanguages").getElemsByTeg("li").flatMap(e ⇒ e.getText.pSplit match{
          case n :: "-" :: l :: v :: _ if n != "" ⇒ {
            val pl = l match{
              case "Basic" ⇒ Some(1)
              case "Conversational" ⇒ Some(2)
              case "Fluent" ⇒ Some(3)
              case "Native" ⇒ Some(4)
              case _ ⇒ None}
            val pv = v match{
              case "Self-Assessed" ⇒ Verified.No
              case "Verified" ⇒ Verified.Yes
              case _ ⇒ Verified.Unknown}
            Some(FreelancerLanguage(n,pl,pv))}
          case _ ⇒ None}),
        photoUrl = {val p = List("oMed p","oContractorInfo oContractorInfoLarge","oMed","oLeft oPortraitLarge")
          om.getElemsByClassPath(p).headOption.flatMap(e ⇒ e).getElemsByTeg("img").headOption.flatMap(e ⇒ e).getAttr("src")},
        rate = pjo.getTopString("contractorRateData","formattedRate").parseDouble,
        rentPercent = pjo.getTopInt("contractorRateData","rentPercent"),
        rating = rw,
        allTimeJobs = njw,
        allTimeHours = hw,
        skills = {val p = List("oMed p","oContractorInfo oContractorInfoLarge","oMed","oBd","oInlineList jsHideWhenEditing")
          om.getElemsByClassPath(p).headOption.flatMap(e ⇒ e).getElemsByTeg("li").flatMap(e ⇒ e.getAttr("data-skill"))},
        companyUrl = cu,
        companyLogoUrl = clu)}
    //List[FreelancerWorkRecord]
    val fws = pja.getTopList("assignments").map(ow ⇒ {
      //Preparing
      val ff = ow.getTopObject("feedback_given")
      val cf = ow.getTopObject("feedback")
      val lp = ow.getTopObject("linkedProject")
      val lpc = lp.getTopObject("pi_category")
      //Building
      FreelancerWorkRecord(
        jobKey = ow.getTopString("enc_job_key"),
        paymentType = ow.getTopString("as_job_type") match{
          case Some("Hourly") ⇒ Payment.Hourly
          case Some("Fixed") ⇒ Payment.Budget
          case _ ⇒ Payment.Unknown},
        status = ow.getTopString("as_status") match{
          case Some("Active") ⇒ Status.Active
          case Some("Closed") ⇒ Status.Closed
          case _ ⇒ Status.Unknown},
        startDate = ow.getTopString("as_from").parseShortDate,
        endDate = ow.getTopString("as_to").parseShortDate,
        fromFull = ow.getTopString("as_from_full").parseJsonDate,
        toFull = ow.getTopString("as_to_full").parseJsonDate,
        openingTitle = ow.getTopString("as_opening_title_original"),
        engagementTitle = ow.getTopString("as_engagement_title"),
        skills =  ow.getTopString("as_skills") match{case Some(s) ⇒ s.split(",").toList; case None ⇒ List()},
        openAccess = ow.getTopString("as_opening_access"),
        cnyStatus = ow.getTopString("as_cny_status"),
        financialPrivacy = ow.getTopString("as_financial_privacy"),
        isHidden = ow.getTopBoolean("isHidden") match{
          case Some(true) ⇒ Hidden.Yes
          case Some(false) ⇒ Hidden.No
          case _ ⇒ Hidden.Unknown},
        agencyName = ow.getTopString("as_agency_name"),
        segmentationData = ow.getTopString("as_segmentation_data"),
        asType = ow.getTopString("as_type"),
        totalHours = ow.getTopString("as_total_hours").parseDouble,
        rate = ow.getTopString("as_rate").parseDouble,
        totalCost = ow.getTopString("as_total_cost").parseDouble,
        chargeRate = ow.getTopString("as_blended_charge_rate").parseDouble,
        amount = ow.getTopString("as_amount").parseDouble,
        totalHoursPrecise = ow.getTopString("as_total_hours_precise").parseDouble,
        costRate = ow.getTopString("as_blended_cost_rate").parseDouble,
        totalCharge = ow.getTopString("as_total_charge").parseDouble,
        ffScores = ff.getTopList("scores").flatMap(e ⇒ {
          (e.getTopString("label"), e.getTopString("score").parseInt) match{
            case (Some(l),Some(s)) ⇒ List((l,s))
            case _ ⇒ List()}}).toMap,
        ffIsPublic = ff.getTopString("comment_is_public"),
        ffComment = ff.getTopString("comment"),
        ffPrivatePoint = ff.getTopString("11point_private_feedback").parseInt,
        ffReasons = ff.getTopString("reasons") match{
          case Some(s) ⇒ s.split(",").map(e ⇒ Some(e).parseInt).toList
          case None ⇒ List()},
        ffResponse = ff.getTopString("response_for_freelancer_feedback"),
        ffScore = ff.getTopString("score").parseDouble,
        cfScores = cf.getTopList("scores").flatMap(e ⇒ {
          (e.getTopString("label"), e.getTopString("score").parseInt) match{
            case (Some(l),Some(s)) ⇒ List((l,s))
            case _ ⇒ List()}}).toMap,
        cfIsPublic = cf.getTopString("comment_is_public"),
        cfComment = cf.getTopString("comment"),
        cfResponse = cf.getTopString("response_for_client_feedback"),
        cfScore = cf.getTopString("score").parseDouble,
        lpTitle = lp.getTopString("pi_title"),
        lpThumbnail = lp.getTopString("pi_thumbnail"),
        lpIsPublic = lp.getTopBoolean("pi_is_public") match{
          case Some(true) ⇒ Public.Yes
          case Some(false) ⇒ Public.No
          case _ ⇒ Public.Unknown},
        lpDescription = lp.getTopString("pi_description"),
        lpRecno = lp.getTopString("pi_recno"),
        lpCatLevel1 = lpc.getTopString("pi_category_level1"),
        lpCatRecno = lpc.getTopInt("pi_category_recno"),
        lpCatLevel2 = lpc.getTopString("pi_category_level2"),
        lpCompleted = lp.getTopString("pi_completed"),
        lpLargeThumbnail = lp.getTopString("pi_large_thumbnail"),
        lpUrl = lp.getTopString("pi_url"),
        lpProjectContractLinkState = lp.getTopString("pi_project_contract_link_state"))})
    //List[FreelancerPortfolioRecord]
    val ps = om.getAllElemsByClass("jsProfileProjectList").headOption.flatMap(e ⇒ e).getElemsByTeg("li").map(op ⇒ {
      FreelancerPortfolioRecord(
        dataId = op.getAttr("data-project-id"),
        title = op.getAllElemsByClass("oRowTitle oProjectTitle oEllipsis").headOption.flatMap(_.getText),
        imgUrl = op.getElemsByClass("oProjectThumbnail").headOption.flatMap(_.getElemsByTeg("img").headOption.flatMap(_.getAttr("src"))))})
    //List[FreelancerTestRecord]
    val tsh = om.getElemsByClassPath(List("jsHideWhenEditing","oTable")).headOption.flatMap(_.getElemsByTeg("tbody").headOption.flatMap(e ⇒ e))
    val ts = tsh.getElemsByTeg("tr").map(ot ⇒ {
      //Preparing
      val (t,s,tm,u) = ot.getElemsByTeg("td") match{
        case t :: s :: tm :: u :: _ ⇒ (t,s,tm,u)
        case _ ⇒ (None,None,None,None)}
      //Building
      FreelancerTestRecord(
        detailsUrl = u.getElemsByTeg("a").headOption.flatMap(_.getAttr("href")),
        title = t.getText,
        score = s.getText.pSplit.headOption.parseDouble,
        timeComplete = tm.getText.pSplit.headOption.parseInt)})
    //List[FreelancerCertification]
    val cs = pjo.getTopArray("contractorCertifications","items").toListOfObj.map(jo ⇒ {
      FreelancerCertification(
        rid = jo.getTopString("rid"),
        name = jo.getTopString("name"),
        customData = jo.getTopString("customData"),
        score = jo.getTopString("score"),
        logoUrl = jo.getTopString("logo_url"),
        certUrl = jo.getTopString("cert_url"),
        isCertVerified = jo.getTopString("is_cert_verified"),
        isVerified = jo.getTopString("is_verified"),
        description = jo.getTopString("description"),
        provider = jo.getTopString("provider"),
        skills = jo.getTopListString("skills"),
        dateEarned = jo.getTopString("date_earned"))})
    //List[FreelancerEmployment]
    val ems = pjo.getTopArray("employmentSection","items").toListOfObj.map(jo ⇒ {
      FreelancerEmployment(
        recordId = jo.getTopString("record_id"),
        title = jo.getTopString("title"),
        company = jo.getTopString("company"),
        dateFrom = jo.getTopLong("start_date").map(d ⇒ new Date(d)),
        dateTo = jo.getTopLong("end_date").map(d ⇒ new Date(d)),
        role = jo.getTopString("role"),
        companyCountry = jo.getTopString("CompanyCountry"),
        companyCity = jo.getTopString("CompanyCity"),
        description = jo.getTopString("description_html"))})
    //List[FreelancerEducation]
    val eds = pjo.getTopArray("educationSection","items").toListOfObj.map(jo ⇒ {
      FreelancerEducation(
        school = jo.getTopString("schoolName"),
        areaOfStudy = jo.getTopString("areaOfStudy"),
        degree = jo.getTopString("degree"),
        dateFrom = jo.getTopString("fromYear").parseYear,
        dateTo = jo.getTopString("toYear").parseYear,
        comments = jo.getTopString("comments"))})
    //List[FreelancerOtherExperience]
    val exs = pjo.getTopArray("contractorOtherExperiencesSection","items").toListOfObj.map(jo ⇒ {
      FreelancerOtherExperience(
        subject = jo.getTopString("subject"),
        description = jo.getTopString("description"))})
    //Return result
    FreelancerParsedData(html,fcd,fws,ps,ts,cs,ems,eds,exs)}
  def parseWorkJson(json:String):FreelancerWorkData = {
    //Current date
    val cd = new Date
    //Parse
    val pj = try{Some(new JSONObject(json))}catch{case _:Exception ⇒ None}
    val co = pj.getTopObject("client")
    val cpo = co.getTopObject("profile")
    //Build
    FreelancerWorkData(
      createDate = cd,
      rawJson = json,
      jobContractorTier = pj.getTopString("contractorTier").parseInt,
      jobSkills = pj match{case Some(j) ⇒ j.getTopListString("skills"); case None => List()},
      jobUrl = pj.getTopString("ciphertext").map(t ⇒ "/jobs/" + t),   // ""/jobs/ + <ciphertext>"
      jobIsPublic = pj.getTopBoolean("isPrivate") match{
        case Some(true) ⇒ Public.No
        case Some(false) ⇒ Public.Yes
        case _ ⇒ Public.Unknown},
      jobDescription = pj.getTopString("description"),
      jobCategory = pj.getTopString("category"),
      jobEndDate = pj.getTopString("endDate").parseDate,
      jobEngagement = pj.getTopString("engagement"),
      jobDuration = pj.getTopString("duration"),
      jobAmount = pj.getTopString("amount").parseDouble,
      clientTotalFeedback = co.getTopString("totalFeedback").parseInt,
      clientScore = co.getTopString("score").parseDouble,
      clientTotalCharge = co.getTopDouble("totalCharge"),
      clientTotalHires = co.getTopString("totalHires").parseInt,
      clientActiveContract = co.getTopInt("activeContract"),
      clientCountry = co.getTopString("country"),
      clientCity = co.getTopString("city"),
      clientTime = co.getTopString("time"),
      clientMemberSince = co.getTopString("memberSince").parseDate,
      clientProfileLogo = cpo.getTopString("logo"),
      clientProfileName = cpo.getTopString("name"),
      clientProfileUrl = cpo.getTopString("url"),
      clientProfileSummary = cpo.getTopString("summary"))}
  def parsePortfolioJson(json:String):FreelancerPortfolioData = {
    //Current date
    val cd = new Date
    //Parse
    val pj = (try{Some(new JSONObject(json))}catch{case _:Exception ⇒ None}).getTopObject("projects")
    //Build
    FreelancerPortfolioData(
      createDate = cd,
      rawJson = json,
      projectDate = pj.getTopString("completionDate").parseJsonShortDate,
      title = pj.getTopString("title"),
      description = pj.getTopString("description"),
      imgUrl = pj.getTopString("thumbnailOriginal"),
      isPublic = pj.getTopBoolean("isPublic") match{
        case Some(false) ⇒ Public.No
        case Some(true) ⇒ Public.Yes
        case _ ⇒ Public.Unknown},
      attachments = pj match{case Some(j) ⇒ j.getTopListString("attachments"); case None => List()},  //<-- ???
      creationTs = pj.getTopString("creationTs").parseJsonFullDate,
      category = pj.getTopString("category"),
      subCategory = pj.getTopString("subcategory"),
      skills =  pj match{case Some(j) ⇒ j.getTopListString("skills"); case None => List()},
      isClient = pj.getTopBoolean("isClient") match{
        case Some(false) ⇒ Client.No
        case Some(true) ⇒ Client.Yes
        case _ ⇒ Client.Unknown},
      flagComment = pj.getTopString("flagComment"),
      projectUrl = pj.getTopString("projectUrl"))}
  def estimateParsingQuality(f:FoundFreelancerRow, opd:Option[FreelancerParsedData]):Double = opd match {
    case Some(pd) ⇒ {
      var r = 1.0
      if(pd.changes.name == None){r -= 1.0}
      if(pd.changes.title == None){r -= 1.0}
      if(pd.changes.overview == None){r -= 1.0}
      if(pd.changes.location == None){r -= 1.0}
      if(pd.changes.availability == FreelancerAvailable.Unknown){r -= 0.2}
      if(pd.changes.languages.isEmpty){r -= 0.2}
      if(pd.changes.rate == None){r -= 0.2}
      if(pd.changes.rating == None){r -= 0.2}
      if(pd.changes.photoUrl == None){r -= 0.2}
      if(pd.changes.skills.isEmpty){r -= 0.2}
      if(pd.changes.timeZone == None){r -= 0.1}
      if(pd.changes.photoUrl == None){r -= 0.1}
      if(pd.changes.title.nonEmpty){
        compareURLAndTitle(f.oUrl, pd.changes.title.get) match{
          case None => {r -= 0.1}
          case Some(false) => {r -= 0.2}
          case _ => }}
      if(r < 0.0){r = 0.0}
      r}
    case _ ⇒ 0.0}}
