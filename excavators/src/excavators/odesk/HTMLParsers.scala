package excavators.odesk
import scala.collection.mutable.{Map => MutMap, Set => MutSet, ListBuffer => MutList}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Set of HTML parsers for oDesk site
 * Created by CAB on 16.09.14.
 */

class HTMLParsers(logger:Logger) {
  //Functions
  private implicit class ElHelper(es:Elements){
    def toList:List[Element] = (for(i <- 0 until es.size())yield es.get(i)).toList}
  //Methods
  def parseWorkSearchResult(html:String):List[FoundWork] = {
    //Parameters
    val jobClassName = "oMed oJobTile jsSimilarTile"
    val titleClassName = "oRowTitle oH3"
    val linkClassName = "oVisitedLink"
    val infoClassName = "oSupportInfo"
    val skillsClassName = "jsSkills oSkills inline"
    val skillClassName = "oSkill oTagSmall oTag"
    val hiresNumClassName = "oFormMsg oFormInfo oHiresNumber p"
    //Parsing
    val d = Jsoup.parse(html)
    //Extract data
    val rl = d.body.getAllElements.toList.filter(e => {e.className() == jobClassName}).map(j => {
      //Ger values
      val ds = j.children().toList.flatMap(e => e.className() match {
        case `titleClassName` => {
          e.children().toList.find(_.className() == linkClassName).map(e => e.attr("href")) match{
            case Some("") | None => List()
            case Some(u) => List(titleClassName -> u)}}
        case `hiresNumClassName` => {
          val as = e.text().split(" ")
          val r = try{Some(as(3).toInt)}catch{case _:Exception => None}
          List(hiresNumClassName -> r)}
        case `infoClassName` => {
          e.children().toList.find(_.className() == skillsClassName).map(se => {
            se.children().toList.filter(_.className() == skillClassName).map(e => e.attr("data-skill"))}) match{
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
        case _ => {
          logger.log("[HTMLParsers.parseWorkSearchResult] Unknown data: " + ds)
          FoundWork("",List(),None)}}})
    //Return result
    rl.filter(_.url != "")}
  def parseJob(html:String):Option[Job] = {





    null
  }














}