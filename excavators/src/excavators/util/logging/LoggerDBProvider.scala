package excavators.util.logging
import java.util.Date

/**
 * Interface which provide DB access for logger
 * Created by CAB on 15.10.2014.
 */
trait LoggerDBProvider {
  def addLogMessage(date:Date, mType:String, name:String, msg:String)}
