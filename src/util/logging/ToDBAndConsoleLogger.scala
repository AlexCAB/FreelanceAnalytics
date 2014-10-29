package excavators.util.logging

import java.text.SimpleDateFormat
import java.util.Date

import excavators.util.logging.LoggerConsole
import excavators.util.parameters.ParametersMap

/**
 * Logger which log to console and to DB
 * Created by CAB on 15.10.2014.
 */

class ToDBAndConsoleLogger(name:String) extends Logger{
  //Parameter
  private var consoleLoggingLevel = List("info","debug","worn","error")
  private var dbLoggingLevel = List("info","debug","worn","error")
  //Helpers
  private val dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
  //Variables
  private var console:Option[LoggerConsole] = None
  private var db:Option[LoggerDBProvider] = None
  //Functions
  private def log(msg:String, tn:String) = {
    val d = new Date
    val sd = dateFormat.format(d)
    console match{
      case Some(c) if (consoleLoggingLevel.contains(tn)) => c.print("[" + tn  + " | " + sd + "] " + msg)
      case _ => println(">> " + sd + ": " + msg)}
    db match{
      case Some(b) if (dbLoggingLevel.contains(tn)) => try{
        val pm = msg.filter(_ != '\n')
        b.addLogMessageRow(d, tn, name, pm)}
        catch{case e:Exception => {
          val t = "[SimpleLogger] Exception on save to DB: " + e
          console match{
            case Some(c) => c.print(t)
            case None => println(">> " + t)}}}
      case _ =>}}
  //Methods
  def setParameters(p:ParametersMap) = {
    consoleLoggingLevel = p.getOrElse("consoleLoggingLevel", {
      log("[ToDBAndConsoleLogger.setParameters]Parameter 'consoleLoggingLevel' not found", "worn")
      consoleLoggingLevel})
    dbLoggingLevel = p.getOrElse("dbLoggingLevel", {
      log("[dbLoggingLevel.setParameters]Parameter 'dbLoggingLevel' not found", "worn")
      dbLoggingLevel})}
  def setConsole(c:LoggerConsole) = {console = Some(c)}
  def setDB(b:LoggerDBProvider) = {db = Some(b)}
  def info(msg:String) = log(msg,"info")
  def debug(msg:String) = log(msg,"debug")
  def worn(msg:String) = log(msg,"worn")
  def error(msg:String) = log(msg,"error")}
