package de.pokerno.form.tournament

trait Rebalance {

  def rebalance(total: Int, n: Int, bucketsCount: Int): Tuple2[Seq[List[Int]], List[Int]] = {
    
    val indexes = List.range(0, total)
    val buckets = indexes.grouped(n).toList
    
    //println("n=", n, "buckets.size=", buckets.size, "bucketsCount=", bucketsCount)
    
    if (buckets.size == bucketsCount) {
      return (buckets, List.empty)
    }
    
    // TODO optimize
    val (result, _last) = buckets.splitAt(bucketsCount)
    var last = _last.flatten
    val (zipping, tail) = result.splitAt(last.size)
    val normalized = zipping.zip(last).map { case (t, x) => x::t }
    if (n == 1) {
      (normalized, tail.flatten)
    } else (normalized ++ tail, List.empty)
  }
  
}
