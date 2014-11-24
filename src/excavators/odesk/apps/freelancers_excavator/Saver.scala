package excavators.odesk.apps.freelancers_excavator

import java.awt.image.BufferedImage
import java.util.Date

import excavators.odesk.db.ODeskExcavatorsDBProvider
import excavators.odesk.parsers.TextHelpers
import util.logging.Logger
import util.parameters.ParametersMap
import util.tasks.{Task, TaskExecutor}
import util.structures._

/**
* Component responsible for parallel save data to DB.
* Created by CAB on 19.11.2014.
*/

class Saver(logger:Logger, db:ODeskExcavatorsDBProvider) extends TaskExecutor{
  //Parameters
  val maxTaskQueueSize = 1000
  val overloadTimeout = 1000
  private var foundFreelancersPriority = 1
  private var nextFreelancerCheckTimeout = 1000 * 60 * 60
  //Variables
  private var saveTime = 0L
  //Functions
  private def checkOverload() = if(queueSize > maxTaskQueueSize){
    logger.worn("[Saver.checkOverload] Queue overload, queueSize = " + queueSize)
    while(isWork && (queueSize > maxTaskQueueSize)){Thread.sleep(overloadTimeout)}}
  private def prepareFreelancerDataToSave(f:FoundFreelancerRow,d:FreelancerParsedData,h:String,ws:Map[String, FreelancerWorkData],
  ps:Map[String,FreelancerPortfolioData],pi:Option[BufferedImage],li:Option[BufferedImage],cis:Map[String,BufferedImage]):AllFreelancerData = {
    val cd = d.changes.createDate
    val fh = FreelancerRowHeader(
      id = -1,
      freelancerId = -1,
      createDate = cd)
    val fr = FreelancerRow(
      id = -1,
      createDate = cd,
      foundDate = f.date,
      oUrl = f.oUrl,
      key = TextHelpers.extractKeyFromURL(f.oUrl).getOrElse(""))
    val rh = FreelancerRawHtmlRow(
      header = fh,
      html = h)
    val rjj = ws.map(e ⇒ FreelancerRawJobJsonRow(
      header = fh,
      json = e._2.rawJson))
    val rpj = ps.map(e ⇒ FreelancerRawPortfolioJsonRow(
      header = fh,
      json = e._2.rawJson))
    val mc = FreelancerMainChangeRow(
      header = fh,
      name = d.changes.name,
      profileAccess = d.changes.profileAccess,
      link = d.changes.link,
      exposeFullName = d.changes.exposeFullName,
      role = d.changes.role,
      videoUrl = d.changes.videoUrl,
      isInviteInterviewAllowed = d.changes.isInviteInterviewAllowed,
      location = d.changes.location,
      timeZone = d.changes.timeZone,
      emailVerified = d.changes.emailVerified,
      photo = pi,
      companyUrl = d.changes.companyLogoUrl,
      companyLogo = li)
    val ac = FreelancerAdditionalChangeRow(
      header = fh,
      title = d.changes.title,
      availability = d.changes.availability,
      availableAgain = d.changes.availableAgain,
      responsivenessScore = d.changes.responsivenessScore,
      overview = d.changes.overview,
      languages = d.changes.languages,
      rate = d.changes.rate,
      rentPercent = d.changes.rentPercent,
      rating = d.changes.rating,
      allTimeJobs = d.changes.allTimeJobs,
      allTimeHours = d.changes.allTimeHours,
      skills = d.changes.skills)
    val fws = d.works.map(wd ⇒ {
      val ad = wd.jobKey.flatMap(jk ⇒ ws.find{case(k,_) ⇒ k == jk}).map(_._2)
      val cpi = ad.flatMap(u ⇒ u.clientProfileLogo).flatMap(u ⇒ cis.find{case(k,_) ⇒ k == u}).map(_._2)
      val w = FreelancerWorkRow(
        header = fh,
        paymentType = wd.paymentType,
        status = wd.status,
        startDate = wd.startDate,
        endDate = wd.endDate,
        fromFull = wd.fromFull,
        toFull = wd.toFull,
        openingTitle = wd.openingTitle,
        engagementTitle = wd.engagementTitle,
        skills = wd.skills,
        openAccess = wd.openAccess,
        cnyStatus = wd.cnyStatus,
        financialPrivacy = wd.financialPrivacy,
        isHidden = wd.isHidden,
        agencyName = wd.agencyName,
        segmentationData = wd.segmentationData) //JSON Array
      val wh = FreelancerWorkRowHeader(
        id = -1,
        freelancerId = -1,
        workId = -1,
        createDate = cd)
      val wad = FreelancerWorkAdditionalDataRow(
        header = wh,
        asType = wd.asType,
        totalHours = wd.totalHours,
        rate = wd.rate,
        totalCost = wd.totalCost,
        chargeRate = wd.chargeRate,
        amount = wd.amount,
        totalHoursPrecise = wd.totalHours,
        costRate = wd.costRate,
        totalCharge = wd.totalCharge,
        jobContractorTier = ad.flatMap(_.jobContractorTier),
        jobUrl = ad.flatMap(_.jobUrl),
        jobDescription = ad.flatMap(_.jobDescription),
        jobCategory = ad.flatMap(_.jobCategory),
        jobEngagement = ad.flatMap(_.jobEngagement),
        jobDuration = ad.flatMap(_.jobDuration),
        jobAmount = ad.flatMap(_.jobAmount))
      val wf = FreelancerWorkFeedbackRow(
        header = wh,
        ffScores = wd.ffScores,
        ffIsPublic = wd.ffIsPublic,
        ffComment = wd.ffComment,
        ffPrivatePoint = wd.ffPrivatePoint,
        ffReasons = wd.ffReasons,
        ffResponse = wd.ffResponse,
        ffScore = wd.ffScore,
        cfScores = wd.cfScores,
        cfIsPublic = wd.cfIsPublic,
        cfComment = wd.cfComment,
        cfResponse = wd.cfResponse,
        cfScore = wd.cfScore)
      val wlp = FreelancerLinkedProjectDataRow(
        header = wh,
        lpTitle = wd.lpTitle,
        lpThumbnail = wd.lpThumbnail,
        lpIsPublic = wd.lpIsPublic,
        lpDescription = wd.lpDescription,
        lpRecno = wd.lpRecno,
        lpCatLevel1 = wd.lpCatLevel1,
        lpCatRecno = wd.lpRecno,
        lpCatLevel2 = wd.lpCatLevel2,
        lpCompleted = wd.lpCompleted,
        lpLargeThumbnail = wd.lpLargeThumbnail,
        lpUrl = wd.lpUrl,
        lpProjectContractLinkState = wd.lpProjectContractLinkState)
      val wc = FreelancerWorkClientRow(
        header = wh,
        clientTotalFeedback = ad.flatMap(_.clientTotalFeedback),
        clientScore = ad.flatMap(_.clientScore),
        clientTotalCharge = ad.flatMap(_.clientTotalCharge),
        clientTotalHires = ad.flatMap(_.clientTotalHires),
        clientActiveContract = ad.flatMap(_.clientActiveContract),
        clientCountry = ad.flatMap(_.clientCountry),
        clientCity = ad.flatMap(_.clientCity),
        clientTime = ad.flatMap(_.clientTime),
        clientMemberSince = ad.flatMap(_.clientMemberSince),
        clientProfileLogo = cpi,
        clientProfileName = ad.flatMap(_.clientProfileName),
        clientProfileUrl = ad.flatMap(_.clientProfileUrl),
        clientProfileSummary = ad.flatMap(_.clientProfileSummary))
        FreelancerWorkDataRow(
          workRow = w,
          workAdditionalDataRow = wad,
          workFeedbackRow = wf,
          linkedProjectDataRow = wlp,
          workClientRow = wc)})
    val fps = d.portfolio.map(p ⇒ {
      val pad = p.dataId.flatMap(pk ⇒ ps.find{case(k,_) ⇒ k == pk}).map(_._2)
      val t = pad.flatMap(d ⇒ d.title) match{case None => p.title; case t:Some[String] ⇒ t}
       FreelancerPortfolioRow(
        header = fh,
        projectDate = pad.flatMap(_.projectDate),
        title = t,
        description = pad.flatMap(_.description),
        isPublic = pad match{case Some(d) ⇒ d.isPublic; case None ⇒ Public.Unknown},
        attachments = pad match{case Some(d) ⇒ d.attachments; case None ⇒ List()},
        creationTs = pad.flatMap(_.creationTs),
        category = pad.flatMap(_.category),
        subCategory = pad.flatMap(_.subCategory),
        skills = pad match{case Some(d) ⇒ d.skills; case None ⇒ List()},
        isClient = pad match{case Some(d) ⇒ d.isClient; case None ⇒ Client.Unknown},
        flagComment = pad.flatMap(_.flagComment),
        projectUrl = pad.flatMap(_.projectUrl),
        imgUrl = pad.flatMap(u ⇒ u.imgUrl))})
    val fts = d.tests.map(t ⇒ FreelancerTestRow(
      header = fh,
      data = t))
    val fcs = d.certification.map(c ⇒ FreelancerCertificationRow(
      header = fh,
      data = c))
    val fes = d.employment.map(e ⇒ FreelancerEmploymentRow(
      header = fh,
      data = e))
    val eds = d.education.map(e ⇒ FreelancerEducationRow(
      header = fh,
      data = e))
    val oes = d.experience.map(e ⇒ FreelancerOtherExperienceRow(
      header = fh,
      data = e))
    val jf = ws.filter{case (_,e) ⇒ e.jobUrl.isDefined}.map{case (_,e) ⇒ {
      FoundJobsRow(
        id = -1,
        oUrl = e.jobUrl.get,
        foundBy = FoundBy.Analyse,
        date = cd,
        priority = 0,
        skills = List(),
        nFreelancers = None)}}
    AllFreelancerData(
      freelancerRow = fr,
      rawHtmlRow = rh,
      rawJobJsonRow = rjj.toList,
      rawPortfolioJsonRow = rpj.toList,
      mainChangeRow = mc,
      additionalChangeRow = ac,
      works = fws,
      portfolioRows = fps,
      testRows = fts,
      certificationRows = fcs,
      employmentRows = fes,
      educationRows = eds,
      otherExperienceRows = oes,
      foundJobsRows = jf.toList)}
  //Tasks
  case class SaveFreelancerDataTask(f:FoundFreelancerRow,pd:FreelancerParsedData,h:String,ws:Map[String, FreelancerWorkData],
  ps:Map[String,FreelancerPortfolioData],pi:Option[BufferedImage],li:Option[BufferedImage], cis:Map[String,BufferedImage]) extends Task(1) {
    def execute() = {
      //Start
      val st = System.currentTimeMillis()
      //Preparing data to save
      val d = prepareFreelancerDataToSave(f,pd,h,ws,ps,pi,li,cis)
      //Save data
      val r = try{
        Some(db.addAllFreelancerDataAndDelFromFound(d))} //Some(number of found jobs)
      catch{case e:Exception ⇒ {
        logger.error("[Saver.SaveFreelancerDataTask] Exception on save data, url: " + f.oUrl + ", exception: " + e)
        None}}
      //If failure on save then del from found els logging
      r match{
        case Some((id, nsj)) ⇒ {
          logger.info("[Saver.SaveFreelancerDataTask] Freelancer data added to DB, name:'"
            + (pd.changes.name match{case Some(t) ⇒ t; case None ⇒ "---"})
            + "',\n  id: " + id + ", url: " + f.oUrl + ",  added " + nsj + " jobs.")}
        case None ⇒ {
          try{
            db.delFoundFreelancerRow(f.id)}
          catch{case e:Exception ⇒ {
            logger.error("[Saver.SaveFreelancerDataTask] Exception on dell wrong save from found: " + e + ", url=" + f.oUrl)}}}}
      //End
      saveTime = System.currentTimeMillis() - st}}
  case class DelFoundFreelancerTas(f:FoundFreelancerRow) extends Task(2) {
    def execute() = {
      //Start
      val st = System.currentTimeMillis()
      //Del
      try{
        db.delFoundFreelancerRow(f.id)}
      catch{case e:Exception ⇒ {
        logger.error("[Saver.DelFoundFreelancerTas] Exception on dell found freelancer: " + e + ", url=" + f.oUrl)}}
      //End
      saveTime = System.currentTimeMillis() - st}}
  //Methods
  def setParameters(p:ParametersMap) = {
    foundFreelancersPriority = p.getOrElse("foundFreelancersPriority", {
      logger.worn("[Worker.setParameters] Parameter 'foundFreelancersPriority' not found.")
      foundFreelancersPriority})
    nextFreelancerCheckTimeout = p.getOrElse("nextFreelancerCheckTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'nextFreelancerCheckTimeout' not found.")
      nextFreelancerCheckTimeout})}
  def addSaveFreelancerDataAndDelFoundTask(f:FoundFreelancerRow,d:FreelancerParsedData,h:String,ws:Map[String, FreelancerWorkData],
    ps:Map[String,FreelancerPortfolioData],pi:Option[BufferedImage],li:Option[BufferedImage],
    cis:Map[String,BufferedImage]) = {
    checkOverload()
    addTask(new SaveFreelancerDataTask(f,d,h,ws,ps,pi,li,cis))}
  def addDelFoundFreelancerTask(f:FoundFreelancerRow) = {
    checkOverload()
    addTask(new DelFoundFreelancerTas(f))}
  def isInProcess:Boolean = (queueSize != 0)
  def getMetrics:(Int,Long) = (queueSize, saveTime)} //Return: (queue size, save time)












