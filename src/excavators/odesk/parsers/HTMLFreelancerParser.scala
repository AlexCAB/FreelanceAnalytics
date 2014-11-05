package excavators.odesk.parsers

import java.util.Date
import org.json.JSONObject
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
  implicit class JSONEx(pj:Map[String, Option[JSONObject]]){
    private def getString(oj:Option[JSONObject], key:String):Option[String] = oj.flatMap(j => {
      try{Some(j.getString(key))}catch{case _:Exception => None}})
    private def getInt(oj:Option[JSONObject], key:String):Option[Int] = oj.flatMap(j => {
      try{Some(j.getInt(key))}catch{case _:Exception => None}})
    def getTopString(jsonName:String,key:String):Option[String] = getString(pj.getOrElse(jsonName,None),key)
    def getTopInt(jsonName:String,key:String):Option[Int] = getInt(pj.getOrElse(jsonName,None),key)
    def getTopDoubleFromString(jsonName:String,key:String):Option[Double] = getString(pj.getOrElse(jsonName,None),key).parseDouble
    def getTopIntFromString(jsonName:String,key:String):Option[Int] = getString(pj.getOrElse(jsonName,None),key).parseInt
    def getStringByPath(jsonName:String, keyPath:List[String]):Option[String] = {
      def srh(oj:Option[JSONObject], ks:List[String]):Option[String] = (oj,ks) match{
        case (oj:Some[JSONObject], k :: Nil) =>  getString(oj, k)
        case (Some(j), k :: t) =>  srh(Some(j.getJSONObject(k)), t)
        case _ => None}
      srh(pj.getOrElse(jsonName,None), keyPath)}





    }


  //Methods
  def parseFreelancerProfile(html:String):FreelancerParsedData = {
    //Current date
    val cd = new Date
    //Parsing of HTML
    val ph = Jsoup.parse(html)
    //Parsing of JSON
    val pt = html.split("odesk.pageConfig.").drop(1).map(s => (s.takeWhile(_ != ' '), s.dropWhile(_ != ' ').drop(3))).toMap
    val pj = pt.filter{case(_,v) => {v != "" && v.dropWhile(_ != ' ').drop(3).take(1) == "{"}}.map{case(k,v) =>
      (k, try{Some(new JSONObject(v))}catch{case _:Exception => None})}
    //FreelancerChanges
    val fcd = {


      //Build
      FreelancerChanges(
        createDate = cd,
        name =  pj.getTopString("contractorTitleData","displayName"),
        link = pj.getTopString("contractorTitleData","link"),
        title = pj.getTopString("contractorTitleData","titleHtml"),
        profileAccess = pj.getStringByPath("contractorAvailability",List("data","profileAccess")),
        exposeFullName = pj.getTopIntFromString("exposeFullName","title"),
        role = pt.getFirstWord("userRole"),
        emailVerified = pt.getBoolean("emailVerified"),
        videoUrl = pj.getTopString("contractorOverviewData","videoUrl"),
        isInviteInterviewAllowed = pt.getBoolean("isInviteInterviewAllowed"),
        availability =  pj.getStringByPath("contractorAvailability",List("data","capacity")) match{
          case Some("fullTime") => FreelancerAvailable.FullTime
          case Some("partTime") => FreelancerAvailable.PartTime
          case Some("lessThen") => FreelancerAvailable.LessThen
          case _ => FreelancerAvailable.Unknown},
        availableAgain = pj.getStringByPath("contractorAvailability",List("data","availableAgain")),
        responsivenessScore = pj.getTopString("contractorAvailability","responsivenessScore"),
        overview = pj.getTopString("contractorOverviewData","overviewHtml"),
        location = Option[String],
        timeZone = Option[Int],  //Shift from +2
        languages = List[FreelancerLanguage],
        photoUrl = Option[String],
        rate = pj.getTopDoubleFromString("contractorRateData","formattedRate"),
        rentPercent = pj.getTopInt("contractorRateData","rentPercent"),
        rating = Option[Double],
        allTimeJobs = Option[Int],
        allTimeHours = Option[Int],
        skills = List[String],
        companyId = Option[Long],
        companyLogoUrl = Option[String])



    }
















    FreelancerParsedData(fcd)}



}
