package sifters.odesk.apps

import java.util.Date
import sifters.odesk.db.ODeskSiftersDBProvider
import util.structures.{FoundBy, FoundJobsRow}

/**
 * Set of general function
 * Created by CAB on 03.11.2014.
 */

class SifterFuns {
  def buildFoundJobsByURL(urls:List[String]):List[FoundJobsRow] = {
    val cd = new Date
    urls.map(url => FoundJobsRow(
      id = -1,
      oUrl = url,
      foundBy = FoundBy.Analyse,
      date = cd,
      priority = 0,      //Default excavator
      skills = List(),
      nFreelancers = None))}
  def deleteJobsByUrl(db:ODeskSiftersDBProvider, urls:List[String]) = {
    val(nj,njc,ncc,na,nh,nw) = db.removeJobsByUrl(urls)
    println("  Deleted from 'odesk_jobs' " + nj + " rows.")
    println("  Deleted from 'odesk_jobs_changes' " + njc + " rows.")
    println("  Deleted from 'odesk_jobs_hired' " + ncc + " rows.")
    println("  Deleted from 'odesk_jobs_applicants' " + na + " rows.")
    println("  Deleted from 'odesk_clients_changes' " + nh + " rows.")
    println("  Deleted from 'odesk_clients_works_history' " + nw + " rows.")}}
