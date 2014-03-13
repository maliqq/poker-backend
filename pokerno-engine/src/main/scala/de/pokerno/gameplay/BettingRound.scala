package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{ msg ⇒ message }

import math.{ BigDecimal ⇒ Decimal }

private[gameplay] class BettingRound(val table: Table, val game: Game, val stake: Stake) extends Round(table.size) {
  current = table.button

  def seats = table.seats.slice(current)
  private var _acting: Option[Tuple2[Seat, Int]] = None
  def acting = _acting
  def acting_=(act: Tuple2[Seat, Int]) {
    _acting = Some(act)
    current = act._2
  }

  def seat: Option[Seat] = _acting.map { _._1 }
  def pos: Option[Int] = _acting.map { _._2 }
  def box: Option[(Player, Int)] = _acting.map { a ⇒
    (a._1.player.get, a._2)
  }

  val pot = new Pot
  var bigBets: Boolean = false

  final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  private var _call: Decimal = .0
  def call = _call
  private var _raise: MinMax = (.0, .0)
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

    val stack = seat.get.stack
    val bet = Bet.forced(betType, List(stack, _call) min)

    addBet(bet)
  }

  def requireBet(act: Tuple2[Seat, Int]) {
    acting = act
    val limit = game.limit

    val blind = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
    val total = seat.get.total

    if (total <= _call || raiseCount >= MaxRaiseCount)
      _raise = (.0, .0)
    else {
      val (min, max) = limit raise (total, blind + _call, pot.total)
      _raise = MinMax(List(total, min) min, List(total, max) min)
    }
  }

  import de.pokerno.util.ConsoleUtils._

  def addBet(_bet: Bet): Bet = {
    val (seat, pos) = _acting.get
    val player = seat.player.get

    var b = if (_bet.betType == Bet.AllIn)
      Bet.raise(seat.total)
    else if (_bet.betType == Bet.Call && _bet.amount.isEmpty)
      _bet.copy(amount = Some(List(_call, seat.total).min - seat.put))
    else _bet

    val valid = b.betType match {
      case Bet.Fold ⇒
        seat.canFold || seat.notActive

      case Bet.Check ⇒
        seat.canCheck(_call)

      case Bet.Call if b.isActive ⇒
        seat.canCall(b.amount.get, _call)

      case Bet.Raise if b.isActive ⇒
        seat.canRaise(b.amount.get, _raise)

      case f: Bet.ForcedBet ⇒
        seat.canForce(b.amount.get, stake.amount(f))

      case _ ⇒
        warn("unmatched bet validation: %s", b)
        false
    }

    if (!valid) {
      warn("bet %s is not valid; call=%.2f raise=%s %s", _bet, _call, _raise, seat)
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
