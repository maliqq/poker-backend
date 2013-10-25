package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.ActorRef
import scala.math.{ BigDecimal ⇒ Decimal }

class BettingRound(val gameplay: Gameplay) extends Round(gameplay.table.size) {
  current = gameplay.table.button

  def seats = gameplay.table.seats.from(current)
  private var _acting: Tuple2[Seat, Int] = null

  def acting = _acting
  def acting_=(act: Tuple2[Seat, Int]) {
    _acting = act
    current = act._2
  }
  def seat = _acting._1
  def pos = _acting._2

  val pot = new Pot
  var bigBets: Boolean = false

  final val MaxRaiseCount = 8
  private var raiseCount: Int = 0

  private var _call: Decimal = .0
  def call = _call
  private var _raise: Range = (.0, .0)
  def raise = _raise

  def clear {
    raiseCount = 0
    _call = .0
    _raise = (.0, .0)
    current = gameplay.table.button
  }
  
  val e = gameplay.events
  def forceBet(act: Tuple2[Seat, Int], betType: Bet.ForcedBet) {
    acting = act

    _call = gameplay.stake amount (betType)

    val stack = seat amount
    val bet = Bet.forced(betType, List(stack, _call) min)

    addBet(bet)
  }

  def requireBet(act: Tuple2[Seat, Int]) {
    acting = act
    val stake = gameplay.stake
    val limit = gameplay.game.limit

    val bb = if (bigBets) stake.bigBlind * 2 else stake.bigBlind
    val stack = seat stack

    if (stack < _call || raiseCount >= MaxRaiseCount)
      _raise = (.0, .0)
    else {
      var (min, max) = limit raise (stack, bb + _call, pot total)
      _raise = Range(List(stack, min) min, List(stack, max) min)
    }

    val player = seat.player get

    e.publish(Message.Acting(pos = current), e.Except(List(player.id)))
    e.publish(Message.RequireBet(pos = current, call = _call, raise = _raise), e.One(player.id))
  }

  def addBet(bet: Bet) {
    val (seat, pos) = _acting
    val player = seat.player get
    
    if (bet.isValid(seat.amount, seat.put, _call, _raise)) {
      val diff = bet.amount - seat.put

      seat post (bet)

      if (bet.betType == Bet.Raise)
        raiseCount += 1

      if (bet.betType != Bet.Call && bet.amount > _call)
        _call = bet.amount

      val left = pot add (player, diff)
      if (seat.state == Seat.AllIn)
        pot split (player, left)
      else
        pot.main add (player, left)

      e.publish(Message.AddBet(pos, bet), e.Except(List(player.id)))
    } else {
      seat fold

      e.publish(Message.AddBet(pos, Bet.fold), e.Except(List(player.id)))
    }
  }

  def complete {
    clear

    gameplay.table.seats where (_ inPlay) map (_._1 play)
    e.publish(Message.CollectPot(total = pot total))
  }

}
