package pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }

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

  case class IsTaken() extends Exception("seat is taken")
}

class Seat {
  private var _state: Seat.State = Seat.Empty
  def state = _state

  private var _amount: Decimal = .0
  def amount = _amount

  private var _put: Decimal = .0
  def put = _put
  def put_=(amount: Decimal) {
    net(_put - amount)
    _put = amount
  }
  
  def stack = _put + _amount

  private var _player: Option[Player] = None
  def player = _player
  def player_=(p: Player) {
    if (_state != Seat.Empty) throw Seat.IsTaken()

    _state = Seat.Taken
    _player = Some(p)
  }

  def clear {
    _state = Seat.Empty
    _player = None
    _amount = .0
    _put = .0
  }

  def play {
    _state = Seat.Play
    _put = .0
  }

  def check {
    _state = Seat.Play
  }

  def fold {
    _state = Seat.Fold
    _put = .0
  }

  def force(amount: Decimal) {
    put = amount
    _state = Seat.Bet
  }

  def raise(amount: Decimal) {
    put = amount
    _state = Seat.Play
  }

  def buyIn(amount: Decimal) {
    net(amount)
    _state = Seat.Ready
  }

  def wins(amount: Decimal) {
    net(amount)
  }

  def isCalled(amount: Decimal): Boolean = {
    _state == Seat.AllIn || amount <= _put
  }

  def post(bet: Bet) = bet.betType match {
    case Bet.Fold             ⇒ fold
    case Bet.Call | Bet.Raise ⇒ raise(bet.amount)
    case Bet.Check => check
    case _: Bet.ForcedBet     ⇒ force(bet.amount)
  }

  private def net(amount: Decimal) {
    _amount += amount
  }

  def isReady = state == Seat.Ready || state == Seat.Play || state == Seat.Fold
  def isActive = state == Seat.Play || state == Seat.PostBB
  def isWaitingBB = state == Seat.WaitBB
  def isPlaying = state == Seat.Play
  def inPlay = state == Seat.Play || state == Seat.Bet
  def inPot = inPlay || state == Seat.AllIn

  override def toString = if (_player.isDefined) "%s - %s (%.2f)".format(_player get, _state, _amount)
    else "(empty)"

}
