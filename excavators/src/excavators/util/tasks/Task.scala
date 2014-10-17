package excavators.util.tasks

/**
 * Abstract task
 * @param time time when task should run
 * @param priority task priority [0,maxInt]
 * Created by CAB on 13.10.2014.
 */

abstract class Task (val time:Long, val priority:Int) {
  def execute():Unit}
