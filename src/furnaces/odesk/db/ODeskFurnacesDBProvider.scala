package furnaces.odesk.db

import java.sql.Timestamp
import java.util.Date
import excavators.odesk.structures._
import excavators.util.logging.LoggerDBProvider
import excavators.util.parameters.ParametersMap
import util.db.DBProvider
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.StaticQuery

/**
 * Provide DB access for oDesk excavators
 * Created by CAB on 29.10.2014.
 */

class ODeskFurnacesDBProvider extends DBProvider{
  //Data methods
  def loadExcavatorsActiveParameters:List[(String,String)] = {
    if(db.isEmpty){throw new Exception("[ODeskExcavatorsDBProvider.saveLogMessage] No created DB.")}
    db.get.withSession(implicit session => {
      excavatorsParamTable.filter(_.is_active === true).map(r => (r.p_key, r.p_value)).list})}




}
