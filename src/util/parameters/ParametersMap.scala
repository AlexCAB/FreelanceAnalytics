package util.parameters
import scala.reflect.runtime.universe._

/**
 * Map to save parameters like key -> value map.
 * Created by CAB on 21.10.2014.
 */

case class ParametersMap(params:Map[String,String]) {
  //Supported classes
  val boolean = typeOf[Boolean]
  val int = typeOf[Int]
  val double = typeOf[Double]
  val long = typeOf[Long]
  val string = typeOf[java.lang.String]
  val list_int = typeOf[List[Int]]
  val list_string = typeOf[List[java.lang.String]]
  //Helpers
  private val intNumbers = Set('0','1','2','3','4','5','6','7','8','9')
  private val doubleNumbers = intNumbers + "."
  //Function
  private def parseInt(s:String):Int = s.filter(intNumbers.contains(_)).toInt
  private def parseLong(s:String):Long = s.filter(intNumbers.contains(_)).toLong
  private def parseDouble(s:String):Double = s.filter(doubleNumbers.contains(_)).toDouble
  private def parseList(s:String):List[String] = s.split("\\|").filter(_ !=  "").toList
  //Methods
  def getOrElse[T:TypeTag](k:String, e: =>T):T = {
    if(params.contains(k)){
      val v = params(k)
      typeOf[T] match {
        case t if t <:< boolean => v match{case "true" => true.asInstanceOf[T]; case "false" => false.asInstanceOf[T]; case _ => e}
        case t if t <:< int => try{parseInt(v).toInt.asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< double => try{parseDouble(v).asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< long => try{parseLong(v).asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< string => try{v.asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< list_int => try{parseList(v).map(parseInt(_)).asInstanceOf[T]}catch{case _:Exception => e}
        case t if t <:< list_string => try{parseList(v).asInstanceOf[T]}catch{case _:Exception => e}
        case _ => throw new Exception("Unsupported type.")}}
    else{
      e}}
  def size:Int = params.size}
