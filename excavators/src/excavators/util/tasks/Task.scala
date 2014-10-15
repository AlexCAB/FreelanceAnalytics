package excavators.util.tasks

/**
 * Abstract task
 * Created by CAB on 13.10.2014.
 */

abstract class Task (val time:Long, val priority:Int) {
  def execute():Unit}
