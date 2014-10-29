package excavators.util.parameters
import org.scalatest._

/**
 * Test for ParametersMap
 * Created by CAB on 21.10.2014.
 */

class ParametersMapTest extends WordSpecLike with Matchers {
  //Preparing
  val param = ParametersMap(Map(
    ("int" -> "123"),
    ("long" -> "321"),
    ("string" -> "str"),
    ("list_int" -> "12|34|56"),
    ("list_string" -> "st1|st2|st3")))
  //Test
  "should int " in {
    val tr1 = param.getOrElse("int", 0)
    val tr2 = param.getOrElse("rrr", 321)
    assert(tr1 == 123)
    assert(tr2 == 321)}
  "should long " in {
    val tr1 = param.getOrElse("long", 0L)
    val tr2 = param.getOrElse("rrr", 123L)
    assert(tr1 == 321)
    assert(tr2 == 123)}
  "should string " in {
    val tr1 = param.getOrElse("string", "")
    val tr2 = param.getOrElse("rrr", "st")
    assert(tr1 == "str")
    assert(tr2 == "st")}
  "should list_int " in {
    val tr1 = param.getOrElse("list_int", List(0))
    val tr2 = param.getOrElse("rrr", List(1,2,3))
    assert(tr1 == List(12,34,56))
    assert(tr2 == List(1,2,3))}
  "should list_string " in {
    val tr1 = param.getOrElse("list_string", List(""))
    val tr2 = param.getOrElse("rrr", List("t1","t2","t3"))
    assert(tr1 == List("st1","st2","st3"))
    assert(tr2 == List("t1","t2","t3"))}}












