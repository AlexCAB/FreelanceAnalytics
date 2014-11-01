//
val to = List((1,0.5),(2,0.5))
val ojs = List(1,1,1,1,1,1,1,1)
val ds = to.map{case(e,d) => (e,(ojs.size * d).toInt)}


val l = ds.last match{case(e,d) => (e,(d + ojs.size - ds.map(_._2).sum))}

val t = ds.dropRight(1) :+ l