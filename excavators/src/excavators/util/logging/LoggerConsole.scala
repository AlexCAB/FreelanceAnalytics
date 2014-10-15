package excavators.util.logging

/**
 * Interface which provide Console access for logger
 * Created by CAB on 21.09.14.
 */

trait LoggerConsole {
  def print(s:String)
  def printLines(ls:List[String])}
