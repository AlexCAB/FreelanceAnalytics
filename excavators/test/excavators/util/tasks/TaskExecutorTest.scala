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


  }
  "should execute task by priority" in {


  }
  "should paused" in{

  }
  "should run after un pause" in{



  }
  "should stop only after end current task" in {




  }





}
