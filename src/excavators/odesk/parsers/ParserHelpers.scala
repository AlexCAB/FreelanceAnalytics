package excavators.odesk.parsers

import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import org.json.{JSONArray, JSONObject}
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Set of parsers helpers
 * Created by CAB on 04.11.2014.
 */

trait ParserHelpers {
  //Attrs
  protected val hrefAttr = "href"
  protected val srcAttr = "src"
  //Tags
  protected val h4Tag = "h4"
  protected val sectionTeg = "section"
  protected val divTag = "div"
  protected val aTeg = "a"
  protected val tbodyTeg = "tbody"
  protected val tableTeg = "table"
  protected val trTeg = "tr"
  protected val thTeg = "th"
  protected val tdTeg = "td"
  protected val iTag = "i"
  protected val strongTeg = "strong"
  protected val imgTag = "img"
  protected val spanTag = "span"
  protected val ulTeg = "ul"
  protected val articleTeg = "article"
  protected val liTeg = "li"
  protected val dataContentTeg = "data-content"
  protected val pTag = "p"
  //Chars
  protected val notInUrlChars = List('\'','-','\"','&','_','~','%','/',',',':','!', '(',')','.','\\','+','$','[',']','*','?','`',';')
  //Helpers HTML
  protected val oDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
  protected val oShortDateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
  protected val oFullDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
  protected val oYearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH)
  protected val oJsonShortDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
  protected val oJsonFullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.ENGLISH)
  protected implicit class ElsHelper(es:Elements){
    def toListOpt:List[Option[Element]] = (for(i <- 0 until es.size())yield es.get(i)).toList.map(Some(_))
    def toList:List[Element] = (for(i <- 0 until es.size())yield es.get(i)).toList}
  protected implicit class ElHelper(oe:Option[Element]){
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
  protected implicit class StringHelper(os:Option[String]){
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
        case s :: _ if(s.contains('/')) => try{Some(oFullDateFormat.parse(s))}catch{case _:Exception => None}
        case _ => None}}
    def parseDate:Option[Date] = os match{
      case s:Some[String] => try{Some(oDateFormat.parse(s.pSplit.mkString(" ")))}catch{case _:Exception => None}
      case None => None}
    def parseYear:Option[Date] = os match{
      case s:Some[String] => try{Some(oYearFormat.parse(s.pSplit.mkString(" ")))}catch{case _:Exception => None}
      case None => None}
    def parseShortDate:Option[Date] = os match{
      case s:Some[String] => try{Some(oShortDateFormat.parse(s.pSplit.mkString(" ")))}catch{case e:Exception => None}
      case None => None}
    def parseJsonDate:Option[Date] = os.flatMap(s => try{Some(oFullDateFormat.parse(s))}catch{case e:Exception => None})
    def parseJsonShortDate:Option[Date] = os.flatMap(s => try{Some(oJsonShortDate.parse(s))}catch{case e:Exception => None})
    def parseJsonFullDate:Option[Date] = os.flatMap(s => {
      val ps = s.replace('T',' ').replace('Z',' ')
      try{Some(oJsonFullDate.parse(ps))}catch{case e:Exception => None}})
    def parseInt:Option[Int] = os match{
      case Some(s) => try{Some(getNum(s).toInt)}catch{case _:Exception => None}
      case None => None}
    def parseDouble:Option[Double] = os match{
      case Some(s) => try{Some(getNum(s).toDouble)}catch{case _:Exception => None}
      case None => None}
    def extractAvgIfIs:Option[Double] = {
      os.pSplit.dropWhile(_ != "(avg").drop(1).headOption.parseDouble}}
  protected implicit class MapHelper(m:Map[String,String]){
    def findByKeyPart(kp:String):Option[String] = {
      m.find{case (k,_) => Some(k).pSplit.contains(kp)}.map{case (_,v) => v}}}
  protected implicit class ElemListHelper(el:List[Option[Element]]){
    def getHead:Option[Element] = el.headOption.flatMap(e => e)}
  //Helpers JSON
  implicit class TextEx(pt:Map[String, String]){
    def getFirstWord(key:String):Option[String] = {
      pt.getOrElse(key,"").takeWhile(_ != ';').filter(c => {c != '\"' && c != ' '}) match{
        case "" => None
        case s => Some(s)}}}
  implicit class JSONObjMapEx(pjo:Map[String, Option[JSONObject]]){
    private def getString(oj:Option[JSONObject], key:String):Option[String] = oj.flatMap(j => {
      (try{Some(j.getString(key))}catch{case _:Exception => None}).flatMap{case "" => None; case s =>Some(s)}})
    private def getInt(oj:Option[JSONObject], key:String):Option[Int] = oj.flatMap(j => {
      try{Some(j.getInt(key))}catch{case _:Exception => None}})
    def getTopString(jsonName:String,key:String):Option[String] = getString(pjo.getOrElse(jsonName,None),key)
    def getTopBoolean(jsonName:String,key:String):Option[Boolean] = pjo.getOrElse(jsonName,None).flatMap(j => {
      try{Some(j.getBoolean(key))}catch{case _:Exception => None}})
    def getTopInt(jsonName:String,key:String):Option[Int] = getInt(pjo.getOrElse(jsonName,None),key)
    def getStringByPath(jsonName:String, keyPath:List[String]):Option[String] = {
      def srh(oj:Option[JSONObject], ks:List[String]):Option[String] = (oj,ks) match{
        case (oj:Some[JSONObject], k :: Nil) =>  getString(oj, k)
        case (Some(j), k :: t) =>  srh(Some(j.getJSONObject(k)), t)
        case _ => None}
      srh(pjo.getOrElse(jsonName,None), keyPath)}
    def getTopArray(jsonName:String,key:String):Option[JSONArray] = pjo.getOrElse(jsonName,None).flatMap(jo => {
      try{Some(jo.getJSONArray(key))}catch{case _:Exception => None}})}
  implicit class JSONArrMapEx(pja:Map[String, Option[JSONArray]]) {
    def getTopList(jsonName: String):List[JSONObject] = {
      pja.getOrElse(jsonName, None) match {
        case Some(jo) => try{(0 until jo.length()).toList.map(i => jo.getJSONObject(i))}catch{case _:Exception => List()}
        case None => List()}}}
  implicit class JSONEx(jo:JSONObject) {
    def getTopString(key:String):Option[String] = {
      try{
        jo.getString(key) match{case "" => None; case s => Some(s)}}
      catch{
        case _:Exception => None}}
    def getTopObject(key:String):Option[JSONObject] = try{Some(jo.getJSONObject(key))}catch{case _:Exception => None}
    def getTopBoolean(key:String):Option[Boolean] = try{Some(jo.getBoolean(key))}catch{case _:Exception => None}
    def getTopLong(key:String):Option[Long] = try{Some(jo.getLong(key))}catch{case _:Exception => None}
    def getTopList(key:String):List[JSONObject] = {
      try{
        val a = jo.getJSONArray(key); (0 until a.length()).toList.map(i => a.getJSONObject(i))}
      catch{
        case _:Exception => List()}}
    def getTopListString(key:String):List[String] = {
      try{
        val a = jo.getJSONArray(key); (0 until a.length()).toList.map(i => a.getString(i))}
      catch{
        case _:Exception => List()}}}
  implicit class JSONOptEx(ojo:Option[JSONObject]) {
    def getTopString(key:String):Option[String] = ojo.flatMap(jo => {
      (try{Some(jo.getString(key))}catch{case _:Exception => None}).flatMap{case "" => None; case s =>Some(s)}})
    def getTopObject(key:String):Option[JSONObject] = ojo.flatMap(jo => try{Some(jo.getJSONObject(key))}catch{case _:Exception => None})
    def getTopBoolean(key:String):Option[Boolean] = ojo.flatMap(jo => try{Some(jo.getBoolean(key))}catch{case _:Exception => None})
    def getTopInt(key:String):Option[Int] = ojo.flatMap(jo => try{Some(jo.getInt(key))}catch{case _:Exception => None})
    def getTopDouble(key:String):Option[Double] = ojo.flatMap(jo => try{Some(jo.getDouble(key))}catch{case _:Exception => None})
    def getTopList(key:String):List[JSONObject] = ojo match{
      case Some(jo) => {
        try{
          val a = jo.getJSONArray(key)
          (0 until a.length()).toList.map(i => a.getJSONObject(i))}
        catch{
          case _:Exception => List()}}
      case None => List()}}
  implicit class JSONArrEx(oja:Option[JSONArray]) {
    def toListOfObj:List[JSONObject] = oja match {
      case Some(ja) => try{(0 until ja.length()).toList.map(i => ja.getJSONObject(i))}catch{case _:Exception => List()}
      case None => List()}}
  //Checkers
  def compareURLAndTitle(url:String, title:String):Option[Boolean] = { //Return: Some(true) if match, Some(false) if no, None if unknown.
  val tws = title.map(c => {if(notInUrlChars.contains(c)) ' ' else c}).split(" ").toSet
    val pws = url.split("/").lastOption.flatMap(_ match{
      case s if s.contains('_') => s.split("_").headOption
      case _ => None})
    pws.map(_.split("-").toSet).flatMap{
      case uws if(uws.nonEmpty && tws.nonEmpty) => Some((uws -- tws).isEmpty)
      case _ => None}}

}
