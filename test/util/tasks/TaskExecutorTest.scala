package util.tasks
import org.scalatest._

/**
 * Test for TaskExecutor class
 * Created by CAB on 20.10.2014.
 */

class TaskExecutorTest extends WordSpecLike with Matchers {
  //Test
  "should start and end" in {
    val ex = new TaskExecutor{}
    ex.start()
    ex.stop()}
  "should execute task" in {
    val ex = new TaskExecutor{}
    ex.start()
    var fl = false
    val t1 = new Task(1){def execute() = {fl = true}}
    ex.addTask(t1)
    Thread.sleep(100)
    assert(fl == true)
    ex.stop()}
  "should execute task by priorityColumn" in {
    val ex = new TaskExecutor{}
    var fl = false
    val t1 = new Task(1){def execute() = {fl = true}}
    val t2 = new Task(2){def execute() = {Thread.sleep(1000)}}
    ex.addTask(t1)
    ex.addTask(t2)
    ex.start()
    Thread.sleep(500)
    assert(fl == false)
    Thread.sleep(1100)
    assert(fl == true)
    ex.stop()}
  "should stop only after end current task" in {
    val ex = new TaskExecutor{}
    ex.start()
    var fl = false
    val ct = System.currentTimeMillis()
    val t1 = new Task(1){def execute() = {
      Thread.sleep(1000)
      fl = true}}
    ex.addTask(t1)
    Thread.sleep(100)
    ex.stop()
    assert(fl == true)
    assert(System.currentTimeMillis() > ct + 900)
    ex.stop()}}

