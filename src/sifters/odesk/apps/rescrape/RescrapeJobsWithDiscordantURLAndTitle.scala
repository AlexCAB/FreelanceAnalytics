package sifters.odesk.apps.rescrape

import excavators.odesk.parsers.HTMLJobParsers
import sifters.odesk.apps.SimpleSifter
import sifters.odesk.db.ODeskSiftersDBProvider


/**
 * Tool search jobs with discordant URL ant title, remove they and add they URLs to rescrape, note:
 * onenote:///D:\Progects\Little%20projects\!Notes\Программы.one#%3eFreelance%20analytics&section-id={2EC25AF8-8B10-4445-B54F-6B56D915AC11}&page-id={55C1974A-B9EB-4EF9-973F-DCA773220EBA}&object-id={ACCE1949-3230-0F4B-34CB-4D08D7900486}&F
 * Created by CAB on 03.11.2014.
 */

object RescrapeJobsWithDiscordantURLAndTitle extends SimpleSifter("RescrapeJobsWithDiscordantURLAndTitle:"){def sift(db:ODeskSiftersDBProvider) = {
  //Helpers
  val html = new HTMLJobParsers
  //Get jobs urls and title
  val urlsAntTitles = db.getJobsUrlAndTitle
  println("  Total " + urlsAntTitles.size + " jobs to check.")
  //Search discordant
  val urls = urlsAntTitles.filter{
    case ((u, Some(t:String))) => html.compareURLAndTitle(u,t) match{case Some(r) => {! r}; case None => false}
    case _ => false}.map(_._1)
  println("  Found " + urls.size + " wrong jobs.")
  //Del wrong
  deleteJobsByUrl(db, urls)
  //Add to rescrape
  val nAdded = db.addFoundJobsRows(buildFoundJobsByURL(urls))
  println("  Added " + nAdded + " jobs to rescrape.")}}
