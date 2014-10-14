package excavators.util.tasks
import org.scalatest._

/**
 * TaskExecutor test
 * Created by WORK on 13.10.2014.
 */

class TaskExecutorTest extends WordSpecLike with Matchers {
  //Task executor
  val executor = new TaskExecutor{}
  //Test
  "should start" in {
    executor.start()}
  "should execute task" in {
    var fl = false
    val t1 = new Task(System.currentTimeMillis(), 1){def execute() = {fl = true}}
    executor.addTask(t1)
    Thread.sleep(100)
    assert(fl == true)}
  "should execute task with time out" in {
    var fl = false
    val t1 = new Task(System.currentTimeMillis() + 2000, 1){def execute() = {fl = true}}
    executor.addTask(t1)
    Thread.sleep(1000)
    assert(fl == false)
    Thread.sleep(1100)
    assert(fl == true)}
  "should execute task by priority" in {
    var fl = false
    val t = System.currentTimeMillis() + 100
    val t1 = new Task(t, 1){def execute() = {fl = true}}
    val t2 = new Task(t, 2){def execute() = {Thread.sleep(1000)}}
    executor.addTask(t1)
    executor.addTask(t2)
    Thread.sleep(500)
    assert(fl == false)
    Thread.sleep(1100)
    assert(fl == true)}
  "should paused and should run after un pause" in{
    var fl = false
    val t = System.currentTimeMillis()
    val t1 = new Task(t + 100, 2){def execute() = {Thread.sleep(500)}}
    val t2 = new Task(t + 200, 1){def execute() = {fl = true}}
    executor.addTask(t1)
    executor.addTask(t2)
    Thread.sleep(400)
    executor.setPaused(true)
    Thread.sleep(400)
    assert(fl == false)
    executor.setPaused(false)
    Thread.sleep(10)
    assert(fl == true)}
  "should stop only after end current task" in {
    var fl = false
    val ct = System.currentTimeMillis()
    val t1 = new Task(ct, 1){def execute() = {
      Thread.sleep(1000)
      fl = true}}
    executor.addTask(t1)
    Thread.sleep(100)
    executor.stop()
    assert(fl == true)
    assert(System.currentTimeMillis() > ct + 900)}}
