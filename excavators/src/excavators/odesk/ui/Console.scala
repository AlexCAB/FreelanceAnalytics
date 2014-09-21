package excavators.odesk.ui

/**
 * Interface for message output
 * Created by CAB on 21.09.14.
 */

trait Console {
  def print(s:String)
  def printLines(ls:List[String])}
