package excavators.util.parameters
import scala.collection.mutable.{Map => MutMap}
import scala.reflect.runtime.universe._

/**
 * Map to save parameters like key -> value map.
 * Created by CAB on 21.10.2014.
 */

case class ParametersMap(params:Map[String,String]) {
  //Supported classes
  val int = typeOf[Int]
  val long = typeOf[Long]
  val string = typeOf[java.lang.String]
  val list_int = typeOf[List[Int]]
  val list_string = typeOf[List[java.lang.String]]
  //Helpers
  private val numbers = List('0','1','2','3','4','5','6','7','8','9')
  //Function
  private def parseNumber(s:String):Long = s.filter(numbers.contains(_)).toLong
  private def parseList(s:String):List[String] = s.split("\\|").filter(_ !=  "").toList
  //Methods
  def getOrElse[T:TypeTag](k:String, e: =>T):T = {
    if(params.contains(k)){
      val v = params(k)
      typeOf[T] match {
        case t if t <:< int => try{parseNumber(v).toInt.asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< long => try{parseNumber(v).asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< string => try{v.asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< list_int => try{parseList(v).map(parseNumber(_)).asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< list_string => try{parseList(v).asInstanceOf[T]}catch{case _:Exception => e}
        case _ => throw new Exception("Unsupported type.")}}
    else{
      e}}}
