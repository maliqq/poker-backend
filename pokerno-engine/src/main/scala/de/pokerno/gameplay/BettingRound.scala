package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{ msg ⇒ message }

import math.{ BigDecimal ⇒ Decimal }

private[gameplay] class BettingRound(val table: Table, val game: Game, val stake: Stake) extends Round(table.size) {
  current = table.button

  def seats = table.seats.slice(current)
  private var _acting: Tuple2[Seat, Int] = null
  def acting = _acting
  def acting_=(act: Tuple2[Seat, Int]) {
    _acting = act
    current = act._2
  }

  def seat = _acting._1
  def pos = _acting._2
  def box = (seat.player.get, pos)

  val pot = new Pot
  var bigBets: Boolean = false

  final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  private var _call: Decimal = .0
  def call = _call
  private var _raise: Range = (.0, .0)
  def raise = _raise

  def clear() {
    raiseCount = 0
    _call = .0
    _raise = (.0, .0)
    current = table.button
    // FIXME
    //pot.complete()
  }

  def forceBet(act: Tuple2[Seat, Int], betType: Bet.ForcedBet): Bet = {
    acting = act

    _call = stake amount betType

    val stack = seat.amount
    val bet = Bet.forced(betType, List(stack, _call) min)

    addBet(bet)
  }

  def requireBet(act: Tuple2[Seat, Int]) {
    acting = act
    val limit = game.limit

    val blind = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
    val stack = seat.stack

    if (stack <= _call || raiseCount >= MaxRaiseCount)
      _raise = (.0, .0)
    else {
      val (min, max) = limit raise (stack, blind + _call, pot.total)
      _raise = Range(List(stack, min) min, List(stack, max) min)
    }
  }

  import de.pokerno.util.ConsoleUtils._

  def addBet(_bet: Bet): Bet = {
    val (seat, pos) = _acting
    val player = seat.player.get

    // alias for raise whole stack
    var b = if (_bet.betType == Bet.AllIn)
      Bet.raise(seat.stack)
    else _bet

    val valid = b.betType match {
      case Bet.Fold ⇒
        seat.canFold

      case Bet.Check ⇒
        seat.canCheck(_call)

      case Bet.Call ⇒
        seat.canCall(b.amount, _call)

      case Bet.Raise ⇒
        seat.canRaise(b.amount, _raise)

      case f: Bet.ForcedBet ⇒
        seat.canForce(b.amount, stake.amount(f))
    }

    if (!valid) {
      warn("bet %s is not valid; call=%.2f raise=%s", _bet, _call, _raise)
      b = Bet.fold
    }

    val diff = seat postBet b

    if (b.isActive) {
      if (b.isRaise)
        raiseCount += 1

      if (!b.isCall && seat.put > _call)
        _call = seat.put

      pot add (player, diff, seat.isAllIn)
    }

    b
  }

}