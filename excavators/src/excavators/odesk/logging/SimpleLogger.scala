package excavators.odesk.logging

import java.util.Date
import excavators.odesk.ui.Console
import java.text.SimpleDateFormat

/**
 * Logger without saving to DB
 * Created by CAB on 21.09.14.
 */

class SimpleLogger extends Logger{
  //Variables
  private var console:Option[Console] = None
  private val dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
  //Methods
  def setConsole(c:Console) = {console = Some(c)}
  def log(msg:String) = {
    val d = dateFormat.format(new Date)
    console match{
      case Some(c) => c.print("[SimpleLogger " + d + "]" + msg)
      case None => println(">> " + d + ": " + msg)}}}


