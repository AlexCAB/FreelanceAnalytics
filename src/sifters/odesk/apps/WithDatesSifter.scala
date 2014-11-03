package sifters.odesk.apps

import java.text.SimpleDateFormat
import java.util.Date
import sifters.odesk.db.ODeskSiftersDBProvider

/**
 * Base class for shifters with two date arguments 'from' and 'to'
 * Created by CAB on 03.11.2014.
 */
abstract class WithDatesSifter(title:String) extends SifterFuns {
  //Helpers
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  //Function
  private def getDate(args:Array[String], i:Int):Option[Date] = {
    if(args.size < (i + 1)){None}else{val a = args(i); if(a == ""){None}else{Some(dateFormat.parse(a))}}}
  //Methods
  def main(args:Array[String]) = {
    println(title)
    //Get arguments
    if(args.size < 4){throw new Exception("  Wrong number of arguments (" + args.size + " instead 4-6)."  )}
    val hostName = args(0)
    val userName = args(1)
    val passwordName = args(2)
    val dbName = args(3)
    val startDate = getDate(args, 4)
    val endDate = getDate(args, 5)
    //Connect to DB
    val db = new ODeskSiftersDBProvider
    println("  Connect to: " + hostName +  ", user: " + userName + ", password: " + passwordName + ", DB: " + dbName)
    db.init(hostName, userName, passwordName, dbName)
    //Sift
    sift(db, startDate, endDate)
    //Halt DB
    db.halt()}
  def sift(db:ODeskSiftersDBProvider, startDate:Option[Date], endDate:Option[Date])
}
