package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}

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

    val bb = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
    val stack = seat.stack

    if (stack < _call || raiseCount >= MaxRaiseCount)
      _raise = (.0, .0)
    else {
      var (min, max) = limit raise (stack, bb + _call, pot.total)
      _raise = Range(List(stack, min) min, List(stack, max) min)
    }
  }
  
  import de.pokerno.util.ConsoleUtils._
  
  def addBet(_bet: Bet): Bet = {
    val (seat, pos) = _acting
    val player = seat.player.get
    
    var bet = if (_bet.isValid(seat.amount, seat.put, _call, _raise))
      _bet
    else {
      warn("bet %s is not valid; call=%.2f raise=%s", _bet, _call, _raise)
      Bet.fold
    }
    
    val _put = seat.put // before posting
    seat post bet

    def postBet() {
      val diff = bet.amount - _put
      
      if (bet.betType == Bet.Raise)
        raiseCount += 1
  
      if (bet.betType != Bet.Call && bet.amount > _call)
        _call = bet.amount
  
      pot add (player, diff, seat.isAllIn)
    }
  
    if (bet.amount != 0) postBet()
    
    bet
  }

}
