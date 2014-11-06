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
  //Helpers
  implicit class TextEx(pt:Map[String, String]){
    def getFirstWord(key:String):Option[String] = {
      pt.getOrElse(key,"").takeWhile(_ != ';').filter(c => {c != '\"' && c != ' '}) match{
        case "" => None
        case s => Some(s)}}
    def getBoolean(key:String):Option[Boolean] = {
      pt.getOrElse(key,"").takeWhile(_ != ';').filter(c => {c != '\"' && c != ' '}) match{
        case "true" => Some(true)
        case "false" => Some(false)
        case _ => None}}


  }
  implicit class JSONObjEx(pjo:Map[String, Option[JSONObject]]){
    private def getString(oj:Option[JSONObject], key:String):Option[String] = oj.flatMap(j => {
      (try{Some(j.getString(key))}catch{case _:Exception => None}).flatMap(_ match{case "" => None; case s =>Some(s)})})
    private def getInt(oj:Option[JSONObject], key:String):Option[Int] = oj.flatMap(j => {
      try{Some(j.getInt(key))}catch{case _:Exception => None}})
    def getTopString(jsonName:String,key:String):Option[String] = getString(pjo.getOrElse(jsonName,None),key)
    def getTopInt(jsonName:String,key:String):Option[Int] = getInt(pjo.getOrElse(jsonName,None),key)
    def getTopDoubleFromString(jsonName:String,key:String):Option[Double] = getString(pjo.getOrElse(jsonName,None),key).parseDouble
    def getTopIntFromString(jsonName:String,key:String):Option[Int] = getString(pjo.getOrElse(jsonName,None),key).parseInt
    def getStringByPath(jsonName:String, keyPath:List[String]):Option[String] = {
      def srh(oj:Option[JSONObject], ks:List[String]):Option[String] = (oj,ks) match{
        case (oj:Some[JSONObject], k :: Nil) =>  getString(oj, k)
        case (Some(j), k :: t) =>  srh(Some(j.getJSONObject(k)), t)
        case _ => None}
      srh(pjo.getOrElse(jsonName,None), keyPath)}
    





    }

  implicit class JSONArrEx(pja:Map[String, Option[JSONArray]]) {
    def getTopList(jsonName: String): List[JSONObject] = {
      pja.getOrElse(jsonName, None) match {
        case Some(jo) => try {
          (0 until jo.length()).toList.map(i => jo.getJSONObject(i))}
        case None => List()}}
  }
  //Methods
  def parseFreelancerProfile(html:String):FreelancerParsedData = {
    //Current date
    val cd = new Date
    //Parsing of HTML
    val ph = try{Some(Jsoup.parse(html).body())}catch{case _:Exception => None}
    //Parsing of JSON
    val pt = html.split("odesk.pageConfig.").drop(1).map(s => {
      val k = s.takeWhile(_ != ' ')
      val v = s.dropWhile(_ != '=').drop(1).dropWhile(_ == ' ')
      (k, v)}).toMap
    val pjo = pt.flatMap{
      case(k,v) if(v != "" && v.take(1) == "{") => Map(k -> (try{Some(new JSONObject(v))}catch{case _:Exception => None}))
      case _ => Map[String,Option[JSONObject]]()}
    val pja = pt.flatMap{
      case(k,v) if(v != "" && v.take(1) == "[") => Map(k -> (try{Some(new JSONArray(v))}catch{case _:Exception => None}))
      case _ => Map[String,Option[JSONArray]]()}

    //Elements
    val om = ph.getAllElemsByClass("oMain").headOption.flatMap(e => e)
    val os = ph.getAllElemsByClass("oSide").headOption.flatMap(e => e)
    //FreelancerChanges
    val fcd = {
      //Preparing
      val tl = om.getAllElemsByClass("oMed jsHideWhenEditing").headOption.flatMap(e => e)
      val (rw,hw,njw) = os.getText.pSplit match {
        case "Work" :: "history" :: r :: h :: _ :: _ :: nj :: _ => (Some(r).parseDouble, Some(h).parseInt, Some(nj).parseInt)
        case _ => (None, None, None)}
      val (cu,clu) = {
        val p = List("oMed p","oImg","oAssocAgencyLogoLink txtMiddle oPortraitSmall")
        val s = os.getElemsByTeg("section").find(e => {val s = e.getText.pSplit.take(2); s.contains("Associated")})
        val a = s.flatMap(e => e.getElemsByClassPath(p).headOption).flatMap(e => e)
        (a.getAttr("href"), a.getElemsByTeg("img").headOption.flatMap(_.getAttr("src")))}
      //Build
      FreelancerChanges(
        createDate = cd,
        name =  pjo.getTopString("contractorTitleData","displayName"),
        link = pjo.getTopString("contractorTitleData","link"),
        title = pjo.getTopString("contractorTitleData","titleHtml"),
        profileAccess = pjo.getStringByPath("contractorAvailability",List("data","profileAccess")),
        exposeFullName = pjo.getTopString("contractorTitleData","exposeFullName"),
        role = pt.getFirstWord("userRole"),
        emailVerified = pt.getBoolean("emailVerified"),
        videoUrl = pjo.getTopString("contractorOverviewData","videoUrl"),
        isInviteInterviewAllowed = pt.getBoolean("isInviteInterviewAllowed"),
        availability =  pjo.getStringByPath("contractorAvailability",List("data","capacity")) match{
          case Some("fullTime") => FreelancerAvailable.FullTime
          case Some("partTime") => FreelancerAvailable.PartTime
          case Some("lessThen") => FreelancerAvailable.LessThen
          case _ => FreelancerAvailable.Unknown},
        availableAgain = pjo.getStringByPath("contractorAvailability",List("data","availableAgain")),
        responsivenessScore = pjo.getTopString("contractorAvailability","responsivenessScore"),
        overview = pjo.getTopString("contractorOverviewData","overviewHtml"),
        location = tl.getElemTextByClassPath(List("oBd","oRowTitle oTextBoxLiner oTxtMed")),
        timeZone = tl.getElemTextByClassPath(List("oBd","oMute")).pSplit.reverse match {
          case "behind" :: _ :: t :: _ => Some(t).parseInt.map(_ * -1)
          case "ahead" :: _ :: t :: _ => Some(t).parseInt
          case _ => None},
        languages = os.findElementByIdOpt("jsLanguages").getElemsByTeg("li").flatMap(e => e.getText.pSplit match{
          case n :: "-" :: l :: v :: _ if n != "" => {
            val pl = l match{
              case "Basic" => Some(1)
              case "Conversational" => Some(2)
              case "Fluent" => Some(3)
              case "Native" => Some(4)
              case _ => None}
            val pv = v match{
              case "Self-Assessed" => Some(false)
              case "Verified" => Some(true)
              case _ => None}
            Some(FreelancerLanguage(n,pl,pv))}
          case _ => None}),
        photoUrl = {val p = List("oMed p","oContractorInfo oContractorInfoLarge","oMed","oLeft oPortraitLarge")
          om.getElemsByClassPath(p).headOption.flatMap(e => e).getElemsByTeg("img").headOption.flatMap(e => e).getAttr("src")},
        rate = pjo.getTopDoubleFromString("contractorRateData","formattedRate"),
        rentPercent = pjo.getTopInt("contractorRateData","rentPercent"),
        rating = rw,
        allTimeJobs = njw,
        allTimeHours = hw,
        skills = {val p = List("oMed p","oContractorInfo oContractorInfoLarge","oMed","oBd","oInlineList jsHideWhenEditing")
          om.getElemsByClassPath(p).headOption.flatMap(e => e).getElemsByTeg("li").flatMap(e => e.getAttr("data-skill"))},
        companyUrl = cu,
        companyLogoUrl = clu)}
















    FreelancerParsedData(fcd,null)}



}
