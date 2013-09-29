package pokerno.backend.poker

object Math {
  final val DefaultSamplesCount: Int = 1000
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
  
  trait AgainstOne {
    val samplesNum: Int
    
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
  
  trait AgainstN {
    val opponentsNum: Int
    var samplesNum: Int
    
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
    
      if (samplesNum == 0) {
        samplesNum = DefaultSamplesCount
      }
      
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
}
