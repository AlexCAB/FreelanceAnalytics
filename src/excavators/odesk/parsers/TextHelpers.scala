package excavators.odesk.parsers

/**
 * Set of helpers text functions
 * Created by CAB on 24.11.2014.
 */

object TextHelpers {
  def extractKeyFromURL(url:String):Option[String] = url match{
    case u if u.contains('%') ⇒ u.split("%").lastOption.map(_.drop(2))
    case u if u.contains('~') ⇒ u.split("~").lastOption
    case _ ⇒ None}}
