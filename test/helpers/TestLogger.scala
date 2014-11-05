package helpers

import util.logging.Logger

/**
 * Dummy logger for test
 * Created by WORK on 18.09.14.
 */

class TestLogger extends Logger{
  def info(msg:String) = println("[TestLogger|info]" + msg)
  def debug(msg:String) = println("[TestLogger|debug]" + msg)
  def worn(msg:String) = println("[TestLogger|worn]" + msg)
  def error(msg:String) = println("[TestLogger|error]" + msg)}
