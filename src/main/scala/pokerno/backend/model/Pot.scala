package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}
import scala.collection.mutable.ListBuffer

class SidePot(val cap: Decimal = .0) {
  var members: Map[Player, Decimal] = Map.empty
  
  def total: Decimal = members.values.sum
  
  def active: Boolean = members.size > 0 && total > .0

  def add(member: Player, amount: Decimal): Decimal = {
    if (amount > .0) {
      val value: Decimal = members.getOrElse(member, .0)
    
      if (cap == .0) {
        members += (member -> (value + amount))
        return .0
      }
    
      if (cap >= amount) {
        members += (member -> cap)
        return value + amount - cap
      }
    }
    
    amount
  }
  
  def split(member: Player, left: Decimal): Tuple2[SidePot, SidePot] = {
    val value: Decimal = members.getOrElse(member, .0)
    val bet = value + left
    members += (member -> bet)
    val main = new SidePot(.0)
    val side = new SidePot(bet)
    
    members foreach { case (key, value) =>
      if (value > bet) {
        if (key != member)
          main.members += (key -> (value - bet))
        side.members += (key -> bet)
      } else
        side.members += (key -> value)
    }
    (main, side)
  }
  
  override def toString = {
    val s = new StringBuilder
    members foreach { case (member, amount) =>
      s ++= "%s: %.2f".format(member, amount)
    }
    s + "-- Total: %.2f Cap: %.2f".format(total, cap)
  }
}

class Pot {
  var main: SidePot = new SidePot(.0)
  var side: List[SidePot] = List.empty
  
  def total: Decimal = sidePots.map( _.total).sum

  def sidePots: List[SidePot] = {
    val pots: ListBuffer[SidePot] = new ListBuffer
    if (main.active)
      pots += main
    side foreach { side =>
      if (side.active)
        pots += side
    }
    pots.toList
  }
  
  def split(member: Player, amount: Decimal) {
    val (_main, _side) = main.split(member, amount)
    side ++= List(_side)
    main = _main
  }
  
  def add(member: Player, amount: Decimal, allIn: Boolean = false) {
    val left = side.foldRight[Decimal](.0) {
      case (p, acc) => p.add(member, acc)
    }
    if (allIn)
      split(member, left)
    else
      main.add(member, left)
  }
  
  override def toString: String = {
    val s = new StringBuilder
    sidePots foreach { sidePot => s ++= sidePot.toString }
    s.toString
  }
}
