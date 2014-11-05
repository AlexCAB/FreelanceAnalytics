package util.tasks

/**
 * Abstract timed task
 * @param time time when task should run (0 to run immediately)
 * @param priority task priorityColumn [0,maxInt]
 * Created by CAB on 13.10.2014.
 */

abstract class TimedTask (val time:Long, val priority:Int) {
  def execute():Unit}
