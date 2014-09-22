package excavators.odesk.db
import org.scalatest._
import java.sql.Date

/**
 * Test for DBProvider class
 * Created by WORK on 22.09.14.
 */

class DBProviderTest extends WordSpecLike with Matchers {
  //Helpers

  //Provider
  val dbProvider = new DBProvider
  //Tests
  "DBProvider must:" must{
    "initialize" in {
      dbProvider.init("jdbc:mysql://127.0.0.1:3306","root","qwerty","freelance_analytics")}
    "add to excavators_log table" in {
      val ct = System.currentTimeMillis()
      dbProvider.addLogMessage(new Date(ct), "m1","Some message 1")
      dbProvider.addLogMessage(new Date(ct), "m2","Some message 2")}









  }}