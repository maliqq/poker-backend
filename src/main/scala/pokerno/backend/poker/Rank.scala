def (r1 Rank) Compare(r2 Rank) int {
  StraightFlush Rank = "StraightFlush" // StraightFlush - straight flush rank
  FourKind      Rank = "FourKind"      // FourKind - four of a kind rank
  FullHouse     Rank = "FullHouse"     // FullHouse - full house rank
  Flush         Rank = "Flush"         // Flush - flush rank
  Straight      Rank = "Straight"      // Straight - straight rank
  ThreeKind     Rank = "ThreeKind"     // ThreeKind - three of a kind rank
  TwoPair       Rank = "TwoPair"       // TwoPair - two pair rank
  OnePair       Rank = "OnePair"       // OnePair - one pair rank
  HighCard      Rank = "HighCard"      // HighCard - high card rank

  BadugiFour  Rank = "BadugiFour"  // BadugiFour - badugi four cards rank
  BadugiThree Rank = "BadugiThree" // BadugiThree - badugi three cards rank
  BadugiTwo   Rank = "BadugiTwo"   // BadugiTwo - badugi two cards rank
  BadugiOne   Rank = "BadugiOne"   // BadugiOne - badugi one card rank

  CompleteLow   Rank = "CompleteLow"   // CompleteLow - complete low rank
  IncompleteLow Rank = "IncompleteLow" // IncompleteLow - incomplete low rank
)

var ranks = map[Rank]int{
  StraightFlush: 0,
  FourKind:      1,
  FullHouse:     2,
  Flush:         3,
  Straight:      4,
  ThreeKind:     5,
  TwoPair:       6,
  OnePair:       7,
  HighCard:      8,

  BadugiFour:  0,
  BadugiThree: 1,
  BadugiTwo:   2,
  BadugiOne:   3,

  CompleteLow:   0,
  IncompleteLow: 1
}

  val a = ranks[r1]
  val b = ranks[r2]

  if (a > b) {
    return -1
  }
  if (a == b) {
    return 0
  }

  return 1
}
