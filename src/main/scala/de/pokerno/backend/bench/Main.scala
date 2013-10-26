package de.pokerno.backend.bench

import de.pokerno.backend.poker.Math
import de.pokerno.backend.poker.Deck

object Main {
  final val opponentsNum = 6
  
  def measure(samplesNum: Int, n: Int = 100) {
    var total: Long = 0
    var min: Long = -1
    var max: Long = 0
    
    for (_<-0 to n) {
      val deck = new Deck
      val cards = deck.deal(2)
      val board = deck.share(3)
      val start = System.currentTimeMillis
      val s = Math.Against(opponentsNum = opponentsNum, samplesNum = samplesNum)
      s.withBoard(cards, board)
      
      val t = System.currentTimeMillis - start 
      total += t
      if (t > max) max = t
      if (min == -1) min = t
      if (t < min) min = t
    }
    
    Console printf("samplesNum=%d\tn=%d\ttotal=%.2fs\tavg=%.3fs\tmin=%.3fs\tmax=%.3fs\n",
        samplesNum, n,
        total.toDouble / 1000,
        total.toDouble / n / 1000,
        min.toDouble / 1000,
        max.toDouble / 1000)
  }
  
  def main(args: Array[String]) {
    measure(1)
    measure(10)
    measure(100)
    measure(1000)
    measure(10000)
    measure(100000)
  }
}
