package sifters.odesk.apps.rescrape

import java.util.Date

import sifters.odesk.apps.SimpleSifter
import sifters.odesk.db.ODeskSiftersDBProvider
import util.structures.{FoundJobsRow, FoundBy}

/**
 * This tool take URLs from 'odesk_excavators_error_pages' and add they to 'odesk_found_jobs' to repeat scraping
 * Created by CAB on 03.11.2014.
 */

object RescrapeFailureParsedJobs extends SimpleSifter("RescrapeFailureParsedJobs:"){def sift(db:ODeskSiftersDBProvider) = {
  //Date
  val cd = new Date
  //Get jobs
  val jobsUrl = db.getUrlOfWrongParsedJobs
  println("  Found " + jobsUrl.size + " wrong parsed jobs.")
  //Prepare
  val preparedJobs = buildFoundJobsByURL(jobsUrl)
  //Add to found table
  val nAdded = db.addFoundJobsRows(preparedJobs)
  println("  Added " + nAdded + " wrong parsed jobs to rescrape")}}
