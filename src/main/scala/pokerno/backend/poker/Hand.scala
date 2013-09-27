package pokerno.backend.poker

object Hand {
  trait Ranking
  
  case object High extends Ranking
  
  case object AceFive extends Ranking
  case object AceFive8 extends Ranking
  case object AceSix extends Ranking
  case object DeuceSix extends Ranking
  case object DeuceSeven extends Ranking
  case object Badugi extends Ranking
}

case class Hand
