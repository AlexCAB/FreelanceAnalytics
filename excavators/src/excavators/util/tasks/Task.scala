package excavators.util.tasks

/**
 * Abstract task
 * Created by WORK on 13.10.2014.
 */

abstract case class Task (time:Long, priority:Int) {
  def execute():Unit}
