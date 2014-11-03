package sifters.odesk.apps

import java.util.Date

import sifters.odesk.db.ODeskSiftersDBProvider
import util.structures.{FoundBy, FoundJobsRow}

/**
 * Base class for simple sifters
 * Created by CAB on 03.11.2014.
 */

abstract class SimpleSifter(title:String) extends SifterFuns{
  def main(args:Array[String]) = {
    println(title)
    //Get arguments
    if(args.size != 4){throw new Exception("  Wrong number of arguments (" + args.size + " instead 4)."  )}
    val hostName = args(0)
    val userName = args(1)
    val passwordName = args(2)
    val dbName = args(3)
    //Connect to DB
    val db = new ODeskSiftersDBProvider
    println("  Connect to: " + hostName +  ", user: " + userName + ", password: " + passwordName + ", DB: " + dbName)
    db.init(hostName, userName, passwordName, dbName)
    //Sift
    sift(db)
    //Halt DB
    db.halt()}
  def sift(db:ODeskSiftersDBProvider)}
