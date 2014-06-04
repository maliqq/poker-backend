package de.pokerno.model

import de.pokerno.poker.Hand
import math.{ BigDecimal ⇒ Decimal }

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonValue, JsonProperty}
import beans._

trait Variation {
  @JsonIgnore def isMixed: Boolean = this.isInstanceOf[Mix]
  @JsonIgnore def tableSize: Int
}

object Game {
  implicit def string2limit(v: String): Option[Limit] = v match {
    case "no-limit" | "nolimit" | "no" ⇒
      Some(NoLimit)
    case "fixed-limit" | "fixedlimit" | "fixed" ⇒
      Some(FixedLimit)
    case "pot-limit" | "potlimit" | "pot" ⇒
      Some(PotLimit)
    case _ ⇒
      None
  }

  trait Limit {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal): Tuple2[Decimal, Decimal]
  }

  case object NoLimit extends Limit {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, total)
    @JsonValue override def toString = "no-limit"
  }

  case object FixedLimit extends Limit {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, bb)
    @JsonValue override def toString = "fixed-limit"
  }

  case object PotLimit extends Limit {
    def raise(total: Decimal, bb: Decimal, potSize: Decimal) = (bb, potSize)
    @JsonValue override def toString = "pot-limit"
  }

  trait Limited

  object Limited {
    implicit def string2Limited(v: String): Option[Limited] = v match {
      case "texas" | "texas-holdem" | "holdem" ⇒
        Some(Texas)
      case "omaha" ⇒
        Some(Omaha)
      case "omaha8" | "omaha-8" ⇒
        Some(Omaha8)
      case "stud" ⇒
        Some(Stud)
      case "stud8" | "stud-8" ⇒
        Some(Stud8)
      case "razz" ⇒
        Some(Razz)
      case "london" ⇒
        Some(London)
      case "five-card" ⇒
        Some(FiveCard)
      case "single27" | "single-27" ⇒
        Some(Single27)
      case "triple27" | "triple-27" ⇒
        Some(Triple27)
      case "badugi" ⇒
        Some(Badugi)
      case _ ⇒
        None
    }
  }

  case object Texas extends Limited {
    @JsonValue override def toString = "texas"
  }
  case object Omaha extends Limited {
    @JsonValue override def toString = "omaha"
  }
  case object Omaha8 extends Limited {
    @JsonValue override def toString = "omaha8"
  }

  case object Stud extends Limited {
    @JsonValue override def toString = "stud"
  }
  case object Stud8 extends Limited {
    @JsonValue override def toString = "stud8"
  }
  case object Razz extends Limited {
    @JsonValue override def toString = "razz"
  }
  case object London extends Limited {
    @JsonValue override def toString = "london"
  }

  case object FiveCard extends Limited {
    @JsonValue override def toString = "five-card"
  }
  case object Single27 extends Limited {
    @JsonValue override def toString = "single27"
  }
  case object Triple27 extends Limited {
    @JsonValue override def toString = "triple27"
  }
  case object Badugi extends Limited {
    @JsonValue override def toString = "badugi"
  }

  trait Mixed

  object Mixed {
    implicit def string2Mixed(v: String): Option[Mixed] = v match {
      case "eight" | "8-game" | "eight-game" ⇒
        Some(Game.Eight)
      case "horse" ⇒
        Some(Game.Horse)
      case _ ⇒ None
    }
  }

  case object Horse extends Mixed {
    @JsonValue override def toString = "horse"
  }
  case object Eight extends Mixed {
    @JsonValue override def toString = "eight"
  }

  trait Group
  case object Holdem extends Group
  case object SevenCard extends Group
  case object SingleDraw extends Group
  case object TripleDraw extends Group

  final val MaxTableSize = 10
  final val FullBoardSize = 5

  case class Options(
      group: Group = Holdem,
      hasBlinds: Boolean = false,
      hasAnte: Boolean = false,
      hasBringIn: Boolean = false,
      hasBoard: Boolean = false,
      hasVela: Boolean = false,

      discards: Boolean = false,
      reshuffle: Boolean = false,

      maxTableSize: Int = MaxTableSize,

      pocketSize: Int = 0,
      streetsNum: Int = 0,
      hiRanking: Option[Hand.Ranking] = None,
      loRanking: Option[Hand.Ranking] = None,

      defaultLimit: Limit = NoLimit) {

    // на случай когда колода задается вручную
    def minDeckSize = {
      var n = maxTableSize * pocketSize
      if (hasBoard) n += 5
      if (discards) n += pocketSize
      n
    }

    override def toString = {
      val b = new StringBuilder

      b.append(" group=%s max-table-size=%d pocket-size=%d streets-num=%d default-limit=%s".format(
        group,
        maxTableSize,
        pocketSize,
        streetsNum,
        defaultLimit))

      if (hiRanking.isDefined) b.append(" hi-ranking=%s", hiRanking.get)
      if (loRanking.isDefined) b.append(" lo-ranking=%s", loRanking.get)

      if (hasBlinds) b.append(" ✓blinds")
      if (hasAnte) b.append(" ✓ante")
      if (hasBringIn) b.append(" ✓bring-in")
      if (hasBoard) b.append(" ✓board")
      if (hasVela) b.append(" ✓vela")
      if (discards) b.append(" ✓discards")
      if (reshuffle) b.append(" ✓reshuffle")

      b.toString
    }
  }
  
  def apply(game: Game.Limited, limit: Game.Limit): Game =
    new Game(game, Some(limit), None)
  
  def apply(game: Game.Limited, limit: Game.Limit, tableSize: Int): Game =
    new Game(game, Some(limit), Some(tableSize))
}

class Game(
    @JsonProperty val game: Game.Limited,
    _limit: Option[Game.Limit] = None,
    _tableSize: Option[Int] = None
  ) extends Variation {
  @JsonIgnore val options = Games(game)
  @JsonIgnore val tableSize: Int = _tableSize match {
    case None ⇒ options.maxTableSize
    case Some(size) ⇒
      if (size > options.maxTableSize)
        options.maxTableSize
      else
        size
  }
  @JsonProperty val limit: Game.Limit = _limit match {
    case None    ⇒ options.defaultLimit
    case Some(l) ⇒ l
  }
  override def toString = "%s %s %s-max" format (game, limit, tableSize)
}
