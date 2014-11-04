package excavators.odesk.parsers

import org.jsoup.Jsoup

/**
 * Set of HTML parsers for freelancers profiles
 * Created by CAB on 04.11.2014.
 */

class HTMLFreelancerParser extends ParserHelpers {
  //Methods
  def parseFreelancerProfile(html:String) = {
    //Parsing
    val d = Jsoup.parse(html)
    //
    println(d)




















  }



}
