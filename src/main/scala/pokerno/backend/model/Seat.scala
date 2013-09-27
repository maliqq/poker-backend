package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

object Seat {
  trait State
  
  case object Empty extends State
  case object Taken extends State
  case object Ready extends State
  case object Play extends State
  case object Bet extends State
  case object Fold extends State
  case object AllIn extends State
}

class Seat {
  private var _state: Seat.State = Seat.Empty
  def state = _state
  def state_=(value: Seat.State) {
    if (_state == Seat.Taken && _state != Seat.Empty)
      throw new Error("seat is taken")
    if (state == Seat.Ready && _state != Seat.Taken)
      return
    if (value == Seat.Bet && _amount == .0)
      _state = Seat.AllIn
      return
    _state = value
  }
  
  private var _amount: Option[Decimal] = None
  def amount = _amount
  def amount_=(value: Decimal) {
    _amount = Some(value)
  }
  
  private var _bet: Option[Decimal] = None
  
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
    _bet = None
  }
  
  def play {
    state = Seat.Play
    _bet = None
  }
  
  def check {
    state = Seat.Play
  }
  
  def fold {
    state = Seat.Fold
    _bet = None
  }
  
  def buyIn(amount: Decimal) {
    state = Seat.Ready
    net(amount)
  }
  
  def net(amount: Decimal) {
    _amount = Some(amount + _amount.getOrElse(.0))
  }
  
  def called(amount: Decimal): Boolean = {
    _state == Seat.AllIn || amount <= _bet.getOrElse(.0)
  }
  
  def bet_=(amount: Decimal) {
    net(-amount + _bet.getOrElse(.0))
    _bet = Some(amount)
    state = Seat.Bet
  }
}

class Seats(size: Int) {
  val seats: List[Seat] = List.fill(size) { new Seat }
  private var _seating: Map[Player, Int] = Map.empty
}
