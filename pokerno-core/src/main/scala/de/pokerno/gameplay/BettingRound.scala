package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}

import math.{ BigDecimal ⇒ Decimal }

class BettingRound(val table: Table, val game: Game, val stake: Stake) extends Round(table.size) {
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
  
  def addBet(bet: Bet): Bet = {
    val (seat, pos) = _acting
    val player = seat.player.get
    
    def postBet() {
      val diff = bet.amount - seat.put
  
      seat post bet
  
      if (bet.betType == Bet.Raise)
        raiseCount += 1
  
      if (bet.betType != Bet.Call && bet.amount > _call)
        _call = bet.amount
  
      val left = pot add (player, diff)
      if (seat.isAllIn)
        pot split (player, left)
      else
        pot.main add (player, left)
    }
    
    if (bet.isValid(seat.amount, seat.put, _call, _raise)) {
      postBet()
      bet
    } else Bet.fold
  }

  def complete() {
    clear()
    table.seatsAsList.filter(_ inPlay) map (_ play)
  }

}
