package excavators.util.logging
import java.util.Date
import java.text.SimpleDateFormat

/**
 * Logger without saving to DB
 * Created by CAB on 21.09.14.
 */

class SimpleLogger() extends Logger{
  //Variables
  private var console:Option[LoggerConsole] = None
  private val dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
  //Functions
  private def log(msg:String, tn:String) = {
    val d = dateFormat.format(new Date)
    console match{
      case Some(c) => c.print("[SimpleLogger " + d + " | "+ tn +"]" + msg)
      case None => println(">> " + d + ": " + msg)}}
  //Methods
  def setConsole(c:LoggerConsole) = {console = Some(c)}
  def info(msg:String) = log(msg,"info")
  def debug(msg:String) = log(msg,"debug")
  def worn(msg:String) = log(msg,"worn")
  def error(msg:String) = log(msg,"error")}




