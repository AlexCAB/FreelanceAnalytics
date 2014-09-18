package test

import scala.collection.mutable.{Map => MutMap, Set => MutSet, ListBuffer => MutList}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


object Test {def main(a:Array[String]) = {
  println("===================================")
  //
  val h = io.Source.fromFile("D:\\Progects\\Little projects\\FreelanceAnalytics\\Dev\\tests\\data\\searchHTML.html").mkString
  //
  val d = Jsoup.parse(h)
//  //
//  def getAllByClassName(re:Element):List[Element] = {
//    val es re.getAllElements
//    var r = MutList[Element]()
//    for(i <- 0 until contentElements.size()){
//      val e = contentElements.get(i)
//      if()
//
//    }





    r.toList}


  val body = d.body()


  val contentElements = body.getAllElements






  for(i <- 0 until contentElements.size()){
    val e = contentElements.get(i)
    if()

  }





}}
