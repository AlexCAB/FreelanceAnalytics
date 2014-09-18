package excavators.helpers

import excavators.odesk.Logger

/**
 * Dummy logger for test
 * Created by WORK on 18.09.14.
 */

class TestLogger extends Logger{
  def log(msg:String) = {
    println("[TestLogger]" + msg)}}
