package excavators.odesk
import scala.collection.mutable.{Map => MutMap, Set => MutSet, ListBuffer => MutList}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.{Locale, Date}
import java.text.SimpleDateFormat

/**
 * Set of HTML parsers for oDesk site
 * Created by CAB on 16.09.14.
 */

class HTMLParsers{
  //Helpers
  private implicit class ElsHelper(es:Elements){
    def toList:List[Element] = (for(i <- 0 until es.size())yield es.get(i)).toList}
  private implicit class ElHelper(e:Element){
    def getChildList:List[Element] = {
      val es = e.children()
      (for(i <- 0 until es.size())yield es.get(i)).toList}
    def getByPath(pl:List[String]):List[Element] = {
      def g(pl:List[String], e:Element):List[Element] = {
        pl match {
          case Nil => List()
          case ph :: pt => {
            val el = e.getChildList.filter(_.className() == ph)
            pt match{
              case Nil => el
              case _ => {
                def l(el:List[Element]):List[Element] = el match{
                  case Nil => List()
                  case eh :: et => {
                    g(pt,eh) match{
                      case Nil => l(et)
                      case lf => lf}}}
                l(el)}}}}}
      g(pl,e)}}
  private def parseTimeAgo(os:Option[String]):Option[Int] = os match{ //Return in min
    case Some(s) => {
      def pi(t:String):Option[Int] = try{Some(t.toInt)}catch{case _:Exception => None}
      s.split(" ").toList match{
        case ws if(ws.contains("minute")) => Some(1)
        case n :: f :: _ if(f == "minutes") => pi(n)
        case ws if(ws.contains("hour")) => Some(60)
        case n :: f :: _ if(f == "hours") => pi(n).map(_ * 60)
        case n :: f :: _ if(f == "day") => pi(n).map(_ * 60 * 24)
        case _ => None}}
    case None => None}
  private val oDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
  private def parseDate(os:Option[String]):Option[Date] = os match{
    case Some(s) => try{Some(oDateFormat.parse(s))}catch{case _:Exception => None}
    case None => None}
  //Methods
  def parseWorkSearchResult(html:String):ParsedSearchResults = {
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
    val refAttr = "href"
    val skillAttr = "data-skill"
    val h4Tag = "h4"
    //Parsing
    val d = Jsoup.parse(html)
    //Extract jobs data
    val ael = d.body.getAllElements.toList
    val rl = ael.filter(e => {e.className() == jobClassName}).map(j => {
      //Ger values
      val ds = j.getChildList.flatMap(e => e.className() match {
        case `titleClassName` => {
          e.getChildList.find(_.className() == linkClassName).map(e => e.attr(refAttr)) match{
            case Some("") | None => List()
            case Some(u) => List(titleClassName -> u)}}
        case `hiresNumClassName` => {
          val as = e.text().split(" ")
          val r = try{Some(as(3).toInt)}catch{case _:Exception => None}
          List(hiresNumClassName -> r)}
        case `infoClassName` => {
          e.getChildList.find(_.className() == skillsClassName).map(se => {
            se.getChildList.filter(_.className() == skillClassName).map(e => e.attr(skillAttr))}) match{
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
      case Some(m) => {
        val f = m.getByPath(numFindPath).map(e => {
          try{
            Some(e.select(h4Tag).first().text().split(" ")(0).filter(_ != ',').toInt)}
          catch{
            case _:Exception => None}})
        val n = m.getByPath(nextFindPath).find(_.text == nextText).map(_.attr(refAttr))
        ((if(f.isEmpty) None else f.head),n)}
      case None => (None,None)}
    //Return result
    ParsedSearchResults(rl,nf,nu)}
  def parseJob(html:String):Option[ParsedJob] = {
    //Parameters
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
    //Functions
    def getElemText(m:Element,p:List[String]):Option[String] = m.getByPath(p).headOption.map(_.text())
    //Parsing
    val d = Jsoup.parse(html).body().getAllElements.toList
    //Get parts
    d.find(_.className() == mainCN).map(m => { //Job data
      val cd = new Date
      //Extract job data
      val ja = d.filter(_.className() == errorCN).exists(e =>{ //Job available?
        val ws = e.text().split(" ")
        ws.contains(availableW) &&  ws.contains(noW)})
      val jpt = getElemText(m, jobPaymentP) match{ //Job payment type
        case Some(s) if(s.split(" ").contains(hourlyW)) => Payment.Hourly
        case Some(s) if(s.split(" ").contains(fixedW))=> Payment.Budget
        case _ => Payment.Unknown}
      val eal = getElemText(m, employmentAndLengthP).map(_.split(" "))
      val j = Job(
        date = cd,
        postDate = m.getByPath(postedP).headOption match{
          case Some(e) => parseTimeAgo(Some(e.getElementById(postedId).text())) match{
            case Some(x) => Some(new Date(cd.getTime - (x * 60 * 1000)))
            case None => None}
          case None => None},
        deadline = parseDate(getElemText(m, deadlineP).map(s => s.split(" ").reverse.take(3).reverse.mkString(" "))),
        daeDate = (if(ja) Some(cd) else None),
        jobTitle = getElemText(m, titleP),
        jobType = getElemText(m, jobTypeP),
        jobPaymentType = jpt,
        jobPrice = getElemText(m, jobPriceP).map(_.filter(c => {c != ',' && c != ' '})) match{
          case Some(s) => try{Some(s.toDouble)}catch{case _:Exception => None}
          case None => None},
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
        jobRequiredLevel = getElemText(m, jobSkillP).map(_.split(" ")) match{
          case Some(ws) if(ws.contains(entryW)) => SkillLevel.Entry
          case Some(ws) if(ws.contains(intermediateW)) => SkillLevel.Intermediate
          case Some(ws) if(ws.contains(expertW)) => SkillLevel.Expert
          case _ => SkillLevel.Unknown},
        jobDescription = m.getElementById(jobDescriptionId) match{
          case e if(e.children().size() != 0) => Some(e.toString)
          case _ => None})
      //Extract changes data





      //   val cp = d.find(_.className() == sideCN) //Client data

      //Return result
      ParsedJob(
        job = j,
        changes = null,
        applicants = List(),
        hires = List(),
        clientWorks = List())})}
  //////////////



}




















































