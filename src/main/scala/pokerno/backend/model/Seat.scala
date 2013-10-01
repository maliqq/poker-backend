package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

object Seat {
  sealed trait State
  
  case object Empty extends State
  case object Taken extends State
  case object Ready extends State
  case object Play extends State
  case object Bet extends State
  case object Fold extends State
  case object AllIn extends State
  case object WaitBB extends State
  case object PostBB extends State
}

class Seat {
  private var _state: Seat.State = Seat.Empty
  def state = _state
  def state_=(value: Seat.State) {
    if (_state == Seat.Taken && _state != Seat.Empty)
      throw new Error("seat is taken")
    if (state == Seat.Ready && _state != Seat.Taken)
      return
    if (value == Seat.Bet && _amount.getOrElse(.0) == .0)
      _state = Seat.AllIn
      return
    _state = value
  }
  
  private var _amount: Option[Decimal] = None
  def amount = _amount
  def amount_=(value: Decimal) {
    _amount = Some(value)
  }
  
  private var _put: Option[Decimal] = None
  def put = _put
  def put_=(amount: Decimal) {
    net(-amount + _put.getOrElse(.0))
    _put = Some(amount)
  }
  
  private var _player: Option[Player] = None
  def player = _player
  def player_=(value: Player) {
    state = Seat.Taken
    _player = Some(value)
  }
  
  def clear {
    state = Seat.Empty
    _player = None
    _amount = None
    _put = None
  }
  
  def play {
    state = Seat.Play
    _put = None
  }
  
  def check = {
    state = Seat.Play
    .0
  }
  
  def fold = {
    state = Seat.Fold
    _put = None
    .0
  }
  
  def isWaitingBB = state == Seat.WaitBB
  def isPlaying = state == Seat.Play
  
  def isReady = state match {
    case Seat.Ready | Seat.Play | Seat.Fold => true
    case _ => false
  }
  
  def isActive = state match {
    case Seat.Play | Seat.PostBB => true
    case _ => false
  }
  
  def inPlay = state match {
    case Seat.Play | Seat.Bet => true
    case _ => false
  }
  
  def inPot = inPlay || state == Seat.AllIn
  
  def force(amount: Decimal) = {
    val d = List[Decimal](amount, _amount.get).min
    put = d
    state = Seat.Bet
    d
  }
  
  def raise(amount: Decimal) = {
    val d = amount - _put.getOrElse(.0)
    put = amount
    state = Seat.Play
    d
  }
  
  def bet(bet: Bet): Decimal = bet.betType match {
    case Bet.Fold => fold
    case Bet.Check => check
    case Bet.Call | Bet.Raise => raise(bet.amount)
    case _: Bet.ForcedBet => force(bet.amount)
  }
  
  def buyIn(amount: Decimal) {
    net(amount)
    state = Seat.Ready
  }
  
  def net(amount: Decimal) {
    _amount = Some(amount + _amount.getOrElse(.0))
  }
  
  def called(amount: Decimal): Boolean = {
    _state == Seat.AllIn || amount <= _put.getOrElse(.0)
  }
}
