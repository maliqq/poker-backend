package pokerno.backend.bench

import pokerno.backend.poker.Math
import pokerno.backend.poker.Deck

object Main {
  final val opponentsNum = 6
  
  def measure(samplesNum: Int) {
    var total: Long = 0
    //for(_ <- 1 to 10) {
      val deck = new Deck
      val cards = deck.deal(2)
      val board = deck.share(3)
      val start = System.currentTimeMillis
      val s = Math.Against(opponentsNum = opponentsNum, samplesNum = samplesNum)
      s.withBoard(cards, board)
      total += System.currentTimeMillis - start
    //}
    val avg = total.toDouble / 1000 // / 10 
    Console printf("samplesNum=%d time=%.3fs\n", samplesNum, avg)
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
