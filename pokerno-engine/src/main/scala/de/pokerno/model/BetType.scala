package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

trait BetType

object BetType {
  case object Raise extends BetType
  case object Call extends BetType
  case object Fold extends BetType
  case object Check extends BetType
  case object AllIn extends BetType
  
  trait Forced extends BetType {
    @JsonValue def name: String
    def apply(amt: Decimal): Bet
  }
  
  object Ante extends Forced {
    def name = "ante"
    def apply(amt: Decimal) = Bet.ante(amt)
  }
  
  object BringIn extends Forced {
    def name = "bring-in"
    def apply(amt: Decimal) = Bet.bringIn(amt)
  }
  
  object SmallBlind extends Forced {
    def name = "small-blind"
    def apply(amt: Decimal) = Bet.sb(amt)
  }
  
  object BigBlind extends Forced {
    def name = "big-blind"
    def apply(amt: Decimal) = Bet.bb(amt)
  }
  
  object GuestBlind extends Forced {
    def name = "guest-blind"
    def apply(amt: Decimal) = Bet.gb(amt)
  }
  
  object Straddle extends Forced {
    def name = "straddle"
    def apply(amt: Decimal) = Bet.straddle(amt)
  }
}
