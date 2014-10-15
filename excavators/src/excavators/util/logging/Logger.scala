package excavators.util.logging

/**
 * Logger interface
 * Created by CAB on 21.09.14.
 */
trait Logger {
  def info(msg:String)
  def debug(msg:String)
  def worn(msg:String)
  def error(msg:String)}

