package excavators.util.logging

import java.text.SimpleDateFormat
import java.util.Date

import excavators.util.logging.LoggerConsole


/**
 * Logger which log to console and to DB
 * Created by CAB on 15.10.2014.
 */
class DBConsoleLogger(name:String) extends Logger{
  //Variables
  private var console:Option[LoggerConsole] = None
  private var db:Option[LoggerDBProvider] = None
  private val dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
  //Functions
  private def log(msg:String, tn:String) = {
    val d = new Date
    val sd = dateFormat.format(d)
    console match{
      case Some(c) => c.print("[" + tn  + " | " + sd + "] " + msg)
      case None => println(">> " + sd + ": " + msg)}
    db match{
      case Some(b) => try{
        b.addLogMessageRow(d, tn, name, msg)}
        catch{case e:Exception => {
          val t = "[SimpleLogger] Exception on save to DB: " + e
          console match{
            case Some(c) => c.print(t)
            case None => println(">> " + t)}}}
      case None =>}}
  //Methods
  def setConsole(c:LoggerConsole) = {console = Some(c)}
  def setDB(b:LoggerDBProvider) = {db = Some(b)}
  def info(msg:String) = log(msg,"info")
  def debug(msg:String) = log(msg,"debug")
  def worn(msg:String) = log(msg,"worn")
  def error(msg:String) = log(msg,"error")}
