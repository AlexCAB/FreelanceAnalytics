package excavators.odesk.apps.freelancers_excavator

import java.awt.image.BufferedImage
import java.util.Date

import excavators.odesk.db.ODeskExcavatorsDBProvider
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
  private var nextJobCheckTimeout = 1000 * 60 * 60
  //Variables
  private var saveTime = 0L
  //Functions
  private def checkOverload() = if(queueSize > maxTaskQueueSize){
    logger.worn("[Saver.checkOverload] Queue overload, queueSize = " + queueSize)
    while(isWork && (queueSize > maxTaskQueueSize)){Thread.sleep(overloadTimeout)}}
  private def prepareJobDataToSave(f:FoundFreelancerRow,d:FreelancerParsedData,h:String,ws:Map[String, FreelancerWorkData],
  ps:Map[String,FreelancerPortfolioData],pi:Option[BufferedImage],li:Option[BufferedImage],
  cis:Map[String,BufferedImage],pis:Map[String,BufferedImage]):AllFreelancerData = {
    val cd = d.changes.createDate

    val fh = FreelancerRowHeader(
                                    id = -1,
                                    freelancerId = -1,
                                    createDate = cd)
//
//    case class FreelancerWorkRowHeader(
//                                        id = Long,
//                                        freelancerId = Long,
//                                        workId = Long,
//                                        createDate = Date)
//

    val fr = FreelancerRow(
                              id = -1,
                              createDate = cd,
                              foundDate = f.date,
                              oUrl = f.oUrl)
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
        createDate = Date)


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
        workClientRow = wc)



    })




    val  = FreelancerPortfolioRow(
                                       header = fh,
                                       projectDate = Option[Date],
                                       title = Option[String],
                                       description = Option[String],
                                       isPublic = Public,
                                       attachments = List[String],
                                       creationTs = Option[Date],
                                       category = Option[String],
                                       subCategory = Option[String],
                                       skills = List[String],
                                       isClient = Client,
                                       flagComment = Option[String],
                                       projectUrl = Option[String],
                                       img = Option[BufferedImage])

    val  = FreelancerTestRow(
                                  header = fh,
                                  data = FreelancerTestRecord)

    val  = FreelancerCertificationRow(
                                           header = fh,
                                           data = FreelancerCertification)

    val  = FreelancerEmploymentRow(
                                        header = fh,
                                        data = FreelancerEmployment)

    val  = FreelancerEducationRow(
                                       header = fh,
                                       data = FreelancerEducation)

    val  = FreelancerOtherExperienceRow(
                                             header = fh,
                                             data = FreelancerOtherExperience)



    val  =

    AllFreelancerData(
                                  freelancerRow = FreelancerRow,
                                  rawHtmlRow = FreelancerRawHtmlRow,
                                  rawJobJsonRow = FreelancerRawJobJsonRow,
                                  rawPortfolioJsonRow = FreelancerRawPortfolioJsonRow,
                                  mainChangeRow = FreelancerMainChangeRow,
                                  additionalChangeRow = FreelancerAdditionalChangeRow,
                                  works = List[FreelancerWorkDataRow],
                                  portfolioRows = List[FreelancerPortfolioRow],
                                  testRows = List[FreelancerTestRow],
                                  certificationRows = List[FreelancerCertificationRow],
                                  employmentRows = List[FreelancerEmploymentRow],
                                  educationRows = List[FreelancerEducationRow],
                                  otherExperienceRows = List[FreelancerOtherExperienceRow],
                                  foundJobsRows = List[FoundJobsRow])






  }
  
  
  
  
  //Tasks
  case class SaveFreelancerDataTask(f:FoundFreelancerRow,pd:FreelancerParsedData,h:String,ws:Map[String, FreelancerWorkData],
  ps:Map[String,FreelancerPortfolioData],pi:Option[BufferedImage],li:Option[BufferedImage],
  cis:Map[String,BufferedImage],pis:Map[String,BufferedImage]) extends Task(1) {
    def execute() = {
      //Start
      val st = System.currentTimeMillis()
      //Preparing data to save
      val d = prepareJobDataToSave(f,pd,h,ws,ps,pi,li,cis,pis)
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
        logger.error("[Saver.DelFoundFreelancerTas] Exception on dell found job: " + e + ", url=" + f.oUrl)}}
      //End
      saveTime = System.currentTimeMillis() - st}}
  //Methods
  def setParameters(p:ParametersMap) = {
    foundFreelancersPriority = p.getOrElse("foundFreelancersPriority", {
      logger.worn("[Worker.setParameters] Parameter 'foundFreelancersPriority' not found.")
      foundFreelancersPriority})
    nextJobCheckTimeout = p.getOrElse("nextJobCheckTimeout", {
      logger.worn("[Worker.setParameters] Parameter 'nextJobCheckTimeout' not found.")
      nextJobCheckTimeout})}
  def addSaveFreelancerDataAndDelFoundTask(f:FoundFreelancerRow,d:FreelancerParsedData,h:String,ws:Map[String, FreelancerWorkData],
    ps:Map[String,FreelancerPortfolioData],pi:Option[BufferedImage],li:Option[BufferedImage],
    cis:Map[String,BufferedImage],pis:Map[String,BufferedImage]) = {
    checkOverload()
    addTask(new SaveFreelancerDataTask(f,d,h,ws,ps,pi,li,cis,pis))}
  def addDelFoundFreelancerTask(f:FoundFreelancerRow) = {
    checkOverload()
    addTask(new DelFoundFreelancerTas(f))}
  def isInProcess:Boolean = (queueSize != 0)
  def getMetrics:(Int,Long) = (queueSize, saveTime)} //Return: (queue size, save time)












