package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.ActorRef
import scala.math.{ BigDecimal => Decimal }

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
  
  override def current_=(newCurrent: Int) {
    super.current_=(newCurrent)
    Console printf("%scurrent=%s seats=%s%s", Console.RED, current, seats.value.map(_._2), Console.RESET)
  }
  
  def forceBet(act: Tuple2[Seat, Int], amount: Decimal) {
    acting = act
    _call = amount
    
    val stack = seat amount
    val bet = Bet.call(List(stack, amount) min)
    
    addBet(bet)
    gameplay.broadcast.all(Message.AddBet(pos, bet))
  }
  
  def requireBet(act: Tuple2[Seat, Int]) {
    acting = act
    val stake = gameplay.stake
    val limit = gameplay.game.limit
    
    val bb = if (bigBets) stake.bigBlind else stake.bigBlind * 2
    val stack = seat stack

    if (stack < _call)
      _raise = (.0, .0)
    else {
      var (min, max) = limit raise (stack, bb + _call, pot total)
      _raise = Range(List(stack, min) min, List(stack, max) min)
    }
    
    val player = seat.player get
    
    gameplay.broadcast.except(player) {
      Message.Acting(pos = current)
    }

    gameplay.broadcast.one(player) {
      Message.RequireBet(pos = current, call = _call, raise = _raise)
    }
  }
  
  def addBet(bet: Bet) {
    val (seat, pos) = _acting
    
    if (bet.isValid(seat.amount, seat.put, _call, _raise)) {
      seat post (bet)
      
      if (bet.betType == Bet.Raise)
        raiseCount += 1

      if (bet.betType != Bet.Call && bet.amount > _call)
        _call = bet.amount

      if (seat.state == Seat.AllIn) pot <<- (seat.player.get, seat.put)
      else pot << (seat.player.get, seat.put)
      
    } else seat fold
  }
  
  def complete {
    clear
    
    gameplay.table.seats where (_ inPlay) map (_._1 play)

    val message = Message.CollectPot(total = pot total)
    gameplay.broadcast all (message)
  }
}
