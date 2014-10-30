package excavators.util.tasks
import org.scalatest._

/**
 * TimedTaskExecutor test
 * Created by CAB on 13.10.2014.
 */

class TimedTaskExecutorTest extends WordSpecLike with Matchers {
  //TimedTask executor
  val executor = new TimedTaskExecutor{}
  //Test
  "should start" in {
    executor.start()}
  "should execute task" in {
    var fl = false
    val t1 = new TimedTask(System.currentTimeMillis(), 1){def execute() = {fl = true}}
    executor.addTask(t1)
    Thread.sleep(100)
    assert(fl == true)}
  "should execute task with time out" in {
    var fl = false
    val t1 = new TimedTask(System.currentTimeMillis() + 2000, 1){def execute() = {fl = true}}
    executor.addTask(t1)
    Thread.sleep(1000)
    assert(fl == false)
    Thread.sleep(1100)
    assert(fl == true)}
  "should execute task by priorityColumn" in {
    var fl = false
    val t = System.currentTimeMillis() + 100
    val t1 = new TimedTask(t, 1){def execute() = {fl = true}}
    val t2 = new TimedTask(t, 2){def execute() = {Thread.sleep(1000)}}
    executor.addTask(t1)
    executor.addTask(t2)
    Thread.sleep(500)
    assert(fl == false)
    Thread.sleep(1100)
    assert(fl == true)}
  "should paused and should run after un pause" in{
    var fl = false
    val t = System.currentTimeMillis()
    val t1 = new TimedTask(t + 100, 2){def execute() = {Thread.sleep(500)}}
    val t2 = new TimedTask(t + 200, 1){def execute() = {fl = true}}
    executor.addTask(t1)
    executor.addTask(t2)
    Thread.sleep(400)
    executor.setPaused(true)
    Thread.sleep(400)
    assert(fl == false)
    executor.setPaused(false)
    Thread.sleep(10)
    assert(fl == true)}
//  "should not add task if already exist" in {
//    var i = 0
//    class ExTask(val ct:Long, p:Int, s:String) extends TimedTask(ct, 1, s){
//      def execute() = {i += 1}}
//    val ct = System.currentTimeMillis() + 100
//    executor.addTask(new ExTask(ct, 1, "qq"))
//    executor.addTask(new ExTask(ct, 2, "qq"))
//    Thread.sleep(400)
//    assert(i == 1)}
  "return number like task" in {
    val ex = new TimedTaskExecutor{}
    class TT1(t:Int) extends TimedTask(t, 1){def execute() = {}}
    class TT2(t:Int) extends TimedTask(t, 1){def execute() = {}}
    //
    ex.addTask(new TT1(0))
    ex.addTask(new TT2(0))
    ex.addTask(new TT1(0))
    ex.addTask(new TT2(0))
    ex.addTask(new TT1(0))
    //
    assert(ex.getNumTaskLike(new TT1(1)) == 3)
    assert(ex.getNumTaskLike(new TT2(2)) == 2)}
  "should stop only after end current task" in {
    var fl = false
    val ct = System.currentTimeMillis()
    val t1 = new TimedTask(ct, 1){def execute() = {
      Thread.sleep(1000)
      fl = true}}
    executor.addTask(t1)
    Thread.sleep(100)
    executor.stop()
    assert(fl == true)
    assert(System.currentTimeMillis() > ct + 900)}}
















































