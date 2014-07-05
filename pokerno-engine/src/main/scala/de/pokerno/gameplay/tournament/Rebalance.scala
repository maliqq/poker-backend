package de.pokerno.gameplay.tournament

trait Rebalance {

  def bucketize(total: Int, perBucket: Int, bucketsCount: Int) = {
    val n = Math.floor(total / perBucket).intValue
    
    val indexes = List.range(0, total)
    val buckets = indexes.grouped(List(n, perBucket).min).toList
    
    println("n=", n, "perBucket=", perBucket, "buckets.size=", buckets.size, "bucketsCount=", bucketsCount)
    // (n=,171,perBucket=,2,buckets.size=,171,bucketsCount=,128)
    
    if (buckets.size > bucketsCount) {
      // TODO optimize
      val (result, _last) = buckets.splitAt(bucketsCount)
      var last = _last.flatten
      val (zipping, tail) = result.splitAt(last.size)
      zipping.zip(last).map{ case (t, x) => x::t } ++ tail
    } else buckets
  }
  
}
