package de.pokerno.gameplay.tournament

trait Rebalance {

  def bucketize(total: Int, perBucket: Int) = List.range(0, total).
    zipWithIndex.
    groupBy { x ⇒ Math.floor(x._2 / total.toDouble * perBucket) }.
    map { case (k, v) ⇒ (k, v.map(_._1)) }

}
