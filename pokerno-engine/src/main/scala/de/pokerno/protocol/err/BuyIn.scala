package de.pokerno.protocol.err

import math.{BigDecimal => Decimal}

object BuyIn {
  object Amount {
    class LessThanMinimum(
      player: Player,
      amount: Decimal,
      min: Decimal
    ) extends Err("buy_in.amount.less_than_minimum") {
      val payload = Map[String, Any](
        "min" -> min,
        "amount" -> amount
      )
      val message = f"player=[$player] has required buy in amount=[$amount] that is less than required amount min=[$min]"
    }

    class GreaterThanMaximum(
      player: Player,
      amount: Decimal,
      max: Decimal
    ) extends Err("buy_in.amount.greater_than_minimum") {
      val payload = Map[String, Any](
        "max" -> max,
        "amount" -> amount
      )
      val message = f"player=[$player] has required buy in amount=[$amount] that is greater than required amount max=[$max]"
    }
  }

  object Stack {
    class EnoughToPlay(
      player: Player,
      stack: Decimal,
      max: Decimal
    ) extends Err("buy_in.stack.enough_to_play") {
      val payload = Map(
        "stack" -> stack,
        "max" -> max
      )
      val message = "player=[$player] has enough stack=[$stack] to play at this table with buy in max=[$max]"
    }
  }

  object Balance {
    class NotEnoughToBuyIn(
      player: Player,
      available: Decimal,
      min: Decimal
    ) extends Err("buy_in.balance.not_enough_to_buy_in"){
      val message = f"player=[$player] has not enough balance=[$available] to join with required buy in min=[$min]"
    }

    class NotEnoughToRebuy(
      player: Player,
      available: Decimal,
      stack: Decimal,
      min: Decimal
    ) extends Err("buy_in.amount.not_enough_to_rebuy") {
      val message = f"player=[$player] has not enough balance=[$available] to rebuy stack=[$stack] to required buy in min=[$min]"
    }
  }
}
