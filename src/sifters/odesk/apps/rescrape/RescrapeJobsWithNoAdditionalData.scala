package sifters.odesk.apps.rescrape

import sifters.odesk.apps.SimpleSifter
import sifters.odesk.db.ODeskSiftersDBProvider

/**
 * Tool which search jobs with no additional data, remove they, and add to rescrape, note:
 * onenote:///D:\Progects\Little%20projects\!Notes\Программы.one#%3eFreelance%20analytics&section-id={2EC25AF8-8B10-4445-B54F-6B56D915AC11}&page-id={55C1974A-B9EB-4EF9-973F-DCA773220EBA}&object-id={CE61318B-9E1D-018F-0094-EE79824B7B90}&B
 * Created by CAB on 03.11.2014.
 */

object RescrapeJobsWithNoAdditionalData extends SimpleSifter("RescrapeJobsWithNoAdditionalData:"){def sift(db:ODeskSiftersDBProvider) = {
  //Get url
  val urls = db.findJobsWithNoAdditionalData
  println("  In 'odesk_jobs' found " + urls.size + " wrong jobs.")
  //Delete
  deleteJobsByUrl(db,urls)
  //Add to rescrape
  val nAdded = db.addFoundJobsRows(buildFoundJobsByURL(urls))
  println("  Added " + nAdded + " jobs to rescrape.")}}