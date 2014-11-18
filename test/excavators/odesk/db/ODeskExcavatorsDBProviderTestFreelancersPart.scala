package excavators.odesk.db
import org.scalatest._
import java.util.Date
import java.awt.image.BufferedImage
import util.structures._

/**
 * Test for ODeskExcavatorsDBProvider class (freelancers part)
 * (!)Test only on empty DB
 * Created by CAB on 13.11.2014.
 */

class ODeskExcavatorsDBProviderTestFreelancersPart extends WordSpecLike with Matchers {
  //Test data
  val cd = new Date
  val freelancerRow = FreelancerRow(
    id = -1,
    createDate = cd,
    oUrl = "http.test.freelancer")
  val freelancerRowHeader = FreelancerRowHeader(
    id = -1,
    freelancerId = 1,
    createDate = cd)
  val freelancerWorkRowHeader = FreelancerWorkRowHeader(
    id = -1,
    freelancerId = 1,
    workId = 1,
    createDate = cd)
  val freelancerRawHtmlRow = FreelancerRawHtmlRow(
    header = freelancerRowHeader,
    html = "<head>Hi</head>")
  val freelancerRawJobJsonRow = FreelancerRawJobJsonRow(
    header = freelancerRowHeader,
    json = "{}")
  val freelancerRawPortfolioJsonRow = FreelancerRawPortfolioJsonRow(
    header = freelancerRowHeader,
    json = "{data:\"data\"}")
  val freelancerMainChangeRow = FreelancerMainChangeRow(
    header = freelancerRowHeader,
    name = Some("name"),
    profileAccess = Some("1"),
    link = Some("Link"),
    exposeFullName = Some("exposeFullName"),
    role = Some("role"),
    videoUrl = Some("videoUrl"),
    isInviteInterviewAllowed = Allowed.Yes,
    location = Some("location"),
    timeZone = Some(123),  //Shift from +2
    emailVerified = Verified.No,
    photo = Some(new BufferedImage(1,2,3)),
    companyUrl = Some("companyUrl"),
    companyLogo = Some(new BufferedImage(1,2,3)))
  val freelancerAdditionalChangeRow = FreelancerAdditionalChangeRow(
    header = freelancerRowHeader,
    title = Some("title"),
    availability = FreelancerAvailable.FullTime,
    availableAgain = Some("availableAgain"),
    responsivenessScore = Some("responsivenessScore"),
    overview = Some("overview"),
    languages = List(
      FreelancerLanguage(
        name = "Ru",
        level = Some(1),
        isVerified = Verified.No),
      FreelancerLanguage(
        name = "Ru",
        level = Some(5),
        isVerified = Verified.Yes)),
    rate = Some(0.5),
    rentPercent = Some(321),
    rating = Some(3.0),
    allTimeJobs = Some(100),
    allTimeHours = Some(1000),
    skills = List("a","b"))
  val freelancerWorkRow = FreelancerWorkRow(
    header = freelancerRowHeader,
    paymentType = Payment.Budget,
    status = Status.Active,
    startDate = Some(cd),
    endDate = Some(cd),
    fromFull = Some(cd),
    toFull = Some(cd),
    openingTitle = Some("openingTitle"),
    engagementTitle = Some("engagementTitle"),
    skills = List("b","c","f"),
    openAccess = Some("openAccess"),
    cnyStatus = Some("cnyStatus"),
    financialPrivacy = Some("financialPrivacy"),
    isHidden = Hidden.No,
    agencyName = Some("agencyName"),
    segmentationData = Some("segmentationData")) //JSON Array
  val freelancerWorkAdditionalDataRow = FreelancerWorkAdditionalDataRow(
    header = freelancerWorkRowHeader,
    asType = Some("asType"),
    totalHours = Some(123),
    rate = Some(0.1),
    totalCost = Some(0.2),
    chargeRate = Some(0.12),
    amount = Some(0.22),
    totalHoursPrecise = Some(0.99),
    costRate = Some(0.77),
    totalCharge = Some(0.66),
    jobContractorTier = Some(1),
    jobUrl = Some("jobUrl"),
    jobDescription = Some("jobDescription"),
    jobCategory = Some("jobCategory"),
    jobEngagement = Some("jobEngagement"),
    jobDuration = Some("jobDuration"),
    jobAmount = Some(0.9999))
  val freelancerWorkFeedbackRow = FreelancerWorkFeedbackRow(
    header = freelancerWorkRowHeader,
    ffScores = Map("1" → 1, "2" → 2, "3" → 3),
    ffIsPublic = Some("ffIsPublic"),
    ffComment = Some("ffComment"),
    ffPrivatePoint = Some(123),
    ffReasons = List(Some(4),Some(1),Some(2)),
    ffResponse = Some("ffResponse"),
    ffScore = Some(0.66),
    cfScores = Map("2" → 1, "5" → 2, "7" → 3),
    cfIsPublic = Some("cfIsPublic"),
    cfComment = Some("cfComment"),
    cfResponse = Some("cfResponse"),
    cfScore = Some(0.646))
  val freelancerLinkedProjectDataRow = FreelancerLinkedProjectDataRow(
    header = freelancerWorkRowHeader,
    lpTitle = Some("lpTitle"),
    lpThumbnail = Some("lpThumbnail"),
    lpIsPublic = Public.Yes,
    lpDescription = Some("lpDescription"),
    lpRecno = Some("lpRecno"),
    lpCatLevel1 = Some("lpCatLevel1"),
    lpCatRecno = Some(11),
    lpCatLevel2 = Some("lpCatLevel2"),
    lpCompleted = Some("lpCompleted"),
    lpLargeThumbnail = Some("lpLargeThumbnail"),
    lpUrl = Some("lpUrl"),
    lpProjectContractLinkState = Some("lpProjectContractLinkState"))
  val freelancerWorkClientRow =FreelancerWorkClientRow(
    header = freelancerWorkRowHeader,
    clientTotalFeedback = Some(6),
    clientScore = Some(4.0),
    clientTotalCharge = Some(9000.1),
    clientTotalHires = Some(12),
    clientActiveContract = Some(2),
    clientCountry = Some("clientCountry"),
    clientCity = Some("clientCity"),
    clientTime = Some("clientTime"),
    clientMemberSince = Some(cd),
    clientProfileLogo = Some(new BufferedImage(3,2,1)),
    clientProfileName = Some("clientProfileName"),
    clientProfileUrl = Some("clientProfileUrl"),
    clientProfileSummary = Some("clientProfileSummary"))
  val freelancerPortfolioRow = FreelancerPortfolioRow(
    header = freelancerRowHeader,
    projectDate = Some(cd),
    title = Some("title"),
    description = Some("description"),
    isPublic = Public.No,
    attachments = List("???","???"),
    creationTs = Some(cd),
    category = Some("category"),
    subCategory = Some("subCategory"),
    skills = List("d","f"),
    isClient = Client.No,
    flagComment = Some("flagComment"),
    projectUrl = Some("projectUrl"),
    img = Some(new BufferedImage(1,2,3)))
  val freelancerTestRecord = FreelancerTestRecord(
    detailsUrl = Some("detailsUrl"),
    title = Some("title"),
    score = Some(234.0),
    timeComplete = Some(987))
  val freelancerCertification = FreelancerCertification(
    rid = Some("rid"),
    name = Some("name"),
    customData = Some("customData"),
    score = Some("score"),
    logoUrl = Some("logoUrl"),
    certUrl = Some("certUrl"),
    isCertVerified = Some("isCertVerified"),
    isVerified = Some("isVerified"),
    description = Some("description"),
    provider = Some("provider"),
    skills = List("g","h"),
    dateEarned = Some("dateEarned"))
  val freelancerEmployment = FreelancerEmployment(
    recordId = Some("recordId"),
    title = Some("title"),
    company = Some("company"),
    dateFrom = Some(cd),
    dateTo = Some(cd),
    role = Some("role"),
    companyCountry = Some("companyCountry"),
    companyCity = Some("companyCity"),
    description = Some("description"))
  val freelancerEducation = FreelancerEducation(
    school = Some("school"),
    areaOfStudy = Some("areaOfStudy"),
    degree = Some("degree"),
    dateFrom = Some(cd),
    dateTo = Some(cd),
    comments = Some("comments"))
  val freelancerOtherExperience = FreelancerOtherExperience(
    subject = Some("subject"),
    description = Some("description"))
  val freelancerTestRow = FreelancerTestRow(
    header = freelancerRowHeader,
    data = freelancerTestRecord)
  val freelancerCertificationRow = FreelancerCertificationRow(
    header = freelancerRowHeader,
    data = freelancerCertification)
  val freelancerEmploymentRow = FreelancerEmploymentRow(
    header = freelancerRowHeader,
    data = freelancerEmployment)
  val freelancerEducationRow = FreelancerEducationRow(
    header = freelancerRowHeader,
    data = freelancerEducation)
  val freelancerOtherExperienceRow = FreelancerOtherExperienceRow(
    header = freelancerRowHeader,
    data = freelancerOtherExperience)
  val foundJobsRow = FoundJobsRow(
    id = -1,
    oUrl = "http.test.job",
    foundBy = FoundBy.Analyse,
    date = cd,
    priority = 0,
    skills = List(),
    nFreelancers = None)
  val freelancerWorkDataRow = FreelancerWorkDataRow(
    workRow = freelancerWorkRow,
    workAdditionalDataRow = freelancerWorkAdditionalDataRow,
    workFeedbackRow = freelancerWorkFeedbackRow,
    linkedProjectDataRow = freelancerLinkedProjectDataRow,
    workClientRow = freelancerWorkClientRow)
  val allFreelancerData = AllFreelancerData(
    freelancerRow = freelancerRow,
    rawHtmlRow = freelancerRawHtmlRow,
    rawJobJsonRow = freelancerRawJobJsonRow,
    rawPortfolioJsonRow = freelancerRawPortfolioJsonRow,
    mainChangeRow = freelancerMainChangeRow,
    additionalChangeRow = freelancerAdditionalChangeRow,
    works = List(freelancerWorkDataRow),
    portfolioRows = List(freelancerPortfolioRow),
    testRows = List(freelancerTestRow),
    certificationRows = List(freelancerCertificationRow),
    employmentRows = List(freelancerEmploymentRow),
    educationRows = List(freelancerEducationRow),
    otherExperienceRows = List(freelancerOtherExperienceRow),
    foundJobsRows = List(foundJobsRow))
  val foundFreelancerRow = FoundFreelancerRow(
    id = -1,
    oUrl = "http.test.freelancer",
    date = cd,
    priority = 1)
  //Provider
  val dbProvider = new ODeskExcavatorsDBProvider
  //Tests
  "initialize" in {
    dbProvider.init("jdbc:mysql://127.0.0.1:3306", "root", "qwerty", "freelance_analytics_test")}
  "add found freelancer" in {
    dbProvider.addFoundFreelancerRow(foundFreelancerRow)}
  "save all freelancer data and delete from found" in {
    val tr = dbProvider.addAllFreelancerDataAndDelFromFound(allFreelancerData)
    assert(tr == 1)}
  "stop" in {
    dbProvider.halt()}}


















































