package pokerno.backend.model
import scala.reflect.runtime.universe._

import scala.math.{BigDecimal => Decimal}

class Bet[T <: Bet.Type : TypeTag](val amount: Decimal = .0) {
  private val _betType = typeOf[T]
  override def toString = {
    if (amount > .0)
      "%s %.2f".format(_betType.toString, amount)
    else
      _betType.toString
  }
}

object Bet {
  type Range = Tuple2[Decimal, Decimal]
  case class Requirement(private var _call: Option[Decimal], private var _raise: Option[Range] = None) {
    def call = _call
    def raise = _raise
    def reset {
      _call = None
      _raise = None
    }
    def disableRaise {
      _raise = None
    }
    def raise_=(value: Range) {
      _raise = Some((_call.get + value._1, _call.get + value._2))
    }
    
    def available_=(value: Decimal) {
      // FIXME .get
      val (min, max) = _raise.get
      
      if (value < max) {
        if (value < _call.get)
          _raise = None
        else if (value < min)
          _raise = Some((value, value))
        else
          _raise = Some((min, value))
      }
    }
  }
  
  trait Type
  
  abstract class ForcedBet extends Type
  abstract class PassiveBet extends Type
  abstract class ActiveBet extends Type
  abstract class DoubleBet extends Type
  abstract class CardAction extends Type
  val doubleBet = manifest[DoubleBet]
  
  case class SmallBlind extends ForcedBet
  val smallBlind = manifest[SmallBlind]
  case class BigBlind extends ForcedBet
  val bigBlind = manifest[BigBlind]
  case class Ante extends ForcedBet
  val ante = manifest[Ante]
  case class BringIn extends ForcedBet
  val bringIn = manifest[BringIn]
  case class GuestBlind extends ForcedBet
  val guestBlind = manifest[GuestBlind]
  case class Straddle extends ForcedBet
  val straddle = manifest[Straddle]
  
  case class Fold extends ActiveBet
  case class Call extends ActiveBet
  
  case class Raise extends PassiveBet
  case class Check extends PassiveBet
  
  case class Discard extends CardAction
  case class StandPat extends CardAction
  case class Show extends CardAction
  case class Muck extends CardAction
  
  def check = new Bet[Check]()
  def fold = new Bet[Fold]()
  def call(amount: Decimal) = new Bet[Call](amount)
  def raise(amount: Decimal) = new Bet[Raise](amount)
  
  def force[T <: ForcedBet : Manifest](stake: Stake): Bet[T] = new Bet[T](stake.amount[T])
}
