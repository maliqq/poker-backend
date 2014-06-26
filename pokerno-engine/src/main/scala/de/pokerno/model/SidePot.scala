package de.pokerno.model

import java.util.Locale
import com.fasterxml.jackson.annotation.JsonValue

class SidePot(var cap: Option[Decimal] = None) {
  // members
  val members = collection.mutable.Map.empty[Player, Decimal].withDefaultValue(0)
  
  //def get(player: Player): Decimal  = members(player)
  def contains(player: Player)      = members.contains(player)
  def isEmpty                       = members.size == 0 || total == 0
  @JsonValue def total: Decimal     = members.values.sum
  //def isActive: Boolean             = members.size > 0 && total > .0

  def add(player: Player, uncalled: Decimal): Decimal = {
    if (uncalled == 0) return 0

    if (cap.isEmpty) {
      members(player) += uncalled
      return 0
    }

    val _cap = cap.get
    val amount = members(player) + uncalled
    if (amount >= _cap) { // (capAmount > value) {
      members(player) = _cap
      return amount - _cap
    }

    uncalled
  }
  
  def extract(player: Player, amount: Decimal): SidePot = {
    val p = new SidePot(Some(amount))
    members.foreach { case (k, v) =>
      val uncalled = p.add(k, v)
      if (uncalled == 0) { // fit in the pot
        members.remove(k) 
      } else {
        members(k) = uncalled
      }
    }
    p
    /*
    player-1: 100 (all-in)
    player-2: 200??
    => (2 pots, CAP)
    player-2: 100
    +[100]
    player-1: 100
    player-2: 100
    ______________________________________
    player-1: 100
    player-2: 1000
    player-3: 1000
    player-4: 1200 (all-in)
    => (1 pot - 0 upper)
    +[1200]
    player-1: 100
    player-2: 1000
    player-3: 1000
    player-4: 1200
    ______________________________________
    player-1: 100
    player-2: 1000 (all-in)
    player-3: 1200 (all-in)
    => (2 pots - 0 upper, CAP)
    player-3: 200 (all-in)
    +[1000]
    player-1: 100
    player-2: 1000 (all-in)
    player-3: 1000
    ______________________________________
    player-1: 400
    player-2: 500
    player-3: 100 (all-in)
    => (2 pots, 2 upper)
    player-1: 300
    player-2: 400
    +[100]
    player-1: 100
    player-2: 100
    player-3: 100(all-in)
    ______________________________________
    player-1: 300(all-in)
    player-2: 200
    player-3: 100(all-in)
    => (2 pots, 2 upper, CAP)
    player-1: 200(all-in)
    player-2: 100
    +[100]
    player-1: 100
    player-2: 100
    player-3: 100(all-in)
     * */
  }
  
  def split(player: Player, uncalled: Decimal): Option[SidePot] = {
    members(player) += uncalled
    
    val value = members(player)
    val upper = members.filter { case (k, v) =>
      k != player && v > value
    }
    if (upper.size == 0 && !cap.isDefined) {
      cap = Some(value)
      return None
    }
    val amount = if (cap.isDefined) {
      val _cap = cap.get
      if (_cap > value) {
        cap = Some(_cap - value)
        value
      } else {
        cap = Some(value - _cap)
        _cap
      }
    } else value
    
    val _side = extract(player, amount)
    Some(_side)
  }
  
  def uncalled(): Option[Tuple2[Player, Decimal]] = {
    if (members.size == 1) {
      return members.headOption
    }
    if (members.size > 1) {
      val sorted = members.toSeq.sortBy(_._2).reverse
      val max = sorted.head // max bet
      val (called, under) = sorted.span(_._2 == max._2) // how much called max bet
      if (called.size == 1)
        return Some(max._1, max._2 - under.head._2)
    }
    None
  }

  override def toString = {
    val s = new StringBuilder

    s.append("{(total: %.2f" formatLocal (Locale.US, total))
    //if (cap.isDefined)
    s.append(" cap: %.2f" formatLocal (Locale.US, cap.getOrElse(.0)))
    s.append(")\n")
    s.append(
      members.map {case (player, amount) ⇒
        "\t%s: %.2f" formatLocal (Locale.US, player, amount)
      }.mkString("\n")
    )
    s.append("\n}")

    s.toString()
  }
}