package pokerno.backend.poker

object Math {
  final val DefaultSamplesNum: Int = 1000
  final val FullBoardLen: Int = 5
  
  class Sample {
    var total: Int = 0
    
    var _wins: Int = 0
    var _ties: Int = 0
    var _loses: Int = 0
    
    def wins: Double = _wins / total
    def ties: Double = _ties / total
    def loses: Double = _loses / total
    
    override def toString = "wins=%.2f ties=%.2f loses=%.2f".format(wins, ties, loses)

    def compare(c1: List[Card], c2: List[Card]) {
      val h1 = Hand.High(c1)
      val h2 = Hand.High(c2)
      
      total += 1
      h1.get.compare(h2.get) match {
      case -1 =>
        _loses += 1
      case 1 =>
        _wins += 1
      case 0 =>
        _ties += 1
      }
    }
  }
  
  case class AgainstOne(val samplesNum: Int = DefaultSamplesNum) {
    def preflop(hole: List[Card], other: List[Card]): Sample = {
      val sample = new Sample
      (0 to samplesNum) foreach { _ =>
        val deck = new Deck without(hole) without(other)
        
        val board = deck.share(5)
        sample.compare(hole ++ board, other ++ board)
      }
      sample
    }
    
    def withBoard(hole: List[Card], board: List[Card]): Sample = {
      if (board.size > 5 || board.size == 0)
        throw new Error("invalid board")

      val deck = new Deck without(hole) without(board)
      val cardsLeft: List[Card] = deck.cards
      
      val sample = new Sample
      for {
        boardVariant <- cardsLeft.combinations(FullBoardLen - board.size);
        other <- cardsLeft.combinations(hole.size)
      } {
        if ((boardVariant.toSet & other.toSet).size == 0) {
          val fullBoard = board ++ boardVariant
          sample.compare(hole ++ fullBoard, other ++ fullBoard)
        }
      }
      sample
    }
  }
  
  case class Headsup(val a: List[Card], val b: List[Card], val samplesNum: Int = DefaultSamplesNum) {
    def withBoard(board: List[Card]) = {
      val deck = new Deck without(a) without(b) without(board)
      val cardsLeft = deck.cards
      val cardsNumToCompleteBoard = FullBoardLen - board.size
    
      val sample = new Sample
      (0 to samplesNum) foreach { _ =>
        val sampleDealer = new Deck(Deck.shuffle(cardsLeft))
        val fullBoard = board ++ sampleDealer.deal(cardsNumToCompleteBoard)
        sample.compare(a ++ fullBoard, b ++ fullBoard)
      }
      sample
    }
  }
  
  case class Against(val opponentsNum: Int, val samplesNum: Int = DefaultSamplesNum) {
    def equity(hole: List[Card], board: List[Card]): Double = {
      var sample = if (board.size == 0)
        preflop(hole)
      else
        withBoard(hole, board)
      sample.wins / samplesNum + sample.ties / opponentsNum
    }
    
    def preflop(hole: List[Card]): Sample = {
      val sample = new Sample
    
      (0 to samplesNum) foreach {_ =>
        val deck = new Deck without hole
        val other = deck.deal(2)
        val board = deck.share(5)
        sample.compare(hole ++ board, other ++ board)
      }
    
      sample
    }
    
    def withBoard(hole: List[Card], board: List[Card]): Sample = {
      if (board.size > 5 || board.size == 0)
        throw new Error("board invalid")
    
      val holeCardsNum = hole.size
      val cardsNumToCompleteBoard = FullBoardLen - board.size
    
      val deck = new Deck without(hole) without(board)
      val cardsLeft = deck.cards
      
      val sample = new Sample
      (0 to samplesNum) foreach { _ =>
        val sampleDealer = new Deck(Deck.shuffle(cardsLeft))
        val fullBoard = board ++ sampleDealer.deal(cardsNumToCompleteBoard)
        (0 to opponentsNum) foreach { _ =>
          val other = sampleDealer.deal(holeCardsNum)
          sample.compare(hole ++ fullBoard, other ++ fullBoard)
        }
      }
      sample
    }
  }

  object Tables {
    final val sklanskyMalmuth = List[Int](
      1, 1, 2, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5,
      2, 1, 2, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      3, 4, 1, 3, 4, 5, 7, 9, 9, 9, 9, 9, 9,
      4, 5, 5, 1, 3, 4, 6, 8, 9, 9, 9, 9, 9,
      6, 6, 6, 5, 2, 4, 5, 7, 9, 9, 9, 9, 9,
      8, 8, 8, 7, 7, 3, 4, 5, 8, 9, 9, 9, 9,
      9, 9, 9, 8, 8, 7, 4, 5, 6, 8, 9, 9, 9,
      9, 9, 9, 9, 9, 9, 8, 5, 5, 6, 8, 9, 9,
      9, 9, 9, 9, 9, 9, 9, 8, 5, 6, 7, 9, 9,
      9, 9, 9, 9, 9, 9, 9, 9, 8, 6, 6, 7, 9,
      9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 7, 7, 8,
      9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 7, 8,
      9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 7
    )
    
    final val evTable = List[Double](
      2.32, 0.77, 0.59, 0.43, 0.33, 0.18, 0.10, 0.08, 0.03, 0.08, 0.06, 0.02, 0.00,
      0.51, 1.67, 0.39, 0.29, 0.20, 0.09, 0.01, 0.00, -0.04, -0.05, -0.05, -0.08, -0.08,
      0.31, 0.16, 1.22, 0.23, 0.17, 0.06, -0.02, -0.06, -0.08, -0.09, -0.10, -0.11, -0.12,
      0.19, 0.07, 0.03, 0.86, 0.15, 0.04, -0.03, -0.07, -0.11, -0.11, -0.11, -0.13, -0.14,
      0.08, 0.01, -0.02, -0.03, 0.58, 0.05, 0.00, -0.05, -0.11, -0.12, -0.13, -0.13, -0.14,
      -0.03, -0.07, -0.08, -0.08, -0.08, 0.38, 0.00, -0.04, -0.09, -0.12, -0.15, -0.14, -0.14,
      -0.07, -0.11, -0.11, -0.10, -0.09, -0.10, 0.25, -0.02, -0.07, -0.11, -0.13, -0.15, -0.14,
      -0.10, -0.11, -0.12, -0.12, -0.10, -0.10, -0.12, 0.16, -0.03, -0.09, -0.11, -0.14, -0.15,
      -0.12, -0.12, -0.13, -0.12, -0.11, -0.12, -0.11, -0.11, 0.07, -0.07, -0.09, -0.11, -0.14,
      -0.12, -0.13, -0.13, -0.13, -0.12, -0.12, -0.11, -0.11, -0.12, 0.02, -0.08, -0.11, -0.14,
      -0.12, -0.13, -0.13, -0.13, -0.12, -0.12, -0.12, -0.12, -0.12, -0.13, -0.03, -0.13, -0.14,
      -0.13, -0.08, -0.13, -0.13, -0.12, -0.12, -0.12, -0.12, -0.12, -0.12, -0.13, -0.07, -0.16,
      -0.15, -0.14, -0.13, -0.13, -0.12, -0.12, -0.12, -0.12, -0.12, -0.12, -0.12, -0.14, -0.09
    )
    
    def sklanskyMalmuthGroup(card1: Card, card2: Card): Int = {
      val index1 = 12 - card1.kind.toInt
      val index2 = 12 - card2.kind.toInt
    
      sklanskyMalmuth(index(index1, index2, card1.suit == card2.suit))
    }
    
    def realPlayStatisticsEV(card1: Card, card2: Card): Double = {
      val index1 = 12 - card1.kind.toInt
      val index2 = 12 - card2.kind.toInt
    
      evTable(index(index1, index2, card1.suit == card2.suit))
    }
    
    def index(index1: Int, index2: Int, suited: Boolean): Int = {
      if (suited) {
        if (index1 > index2)
          return 13 * index2 + index1
        return 13 * index1 + index2
      }
      
      if (index1 > index2) {
        return 13 * index1 + index2
      }
      return 13 * index2 + index1
    }
  }
}
