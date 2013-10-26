package de.pokerno.model

import de.pokerno.poker.Hand
import scala.math.{ BigDecimal ⇒ Decimal }

trait Variation {
  def isMixed: Boolean = this.isInstanceOf[Mix]
  def tableSize: Int
}

object Game {
  trait Limit {
    def raise(stack: Decimal, bb: Decimal, potSize: Decimal): Tuple2[Decimal, Decimal]
  }

  case object NoLimit extends Limit {
    def raise(stack: Decimal, bb: Decimal, potSize: Decimal) = (bb, stack)
  }

  case object FixedLimit extends Limit {
    def raise(stack: Decimal, bb: Decimal, potSize: Decimal) = (bb, bb)
  }

  case object PotLimit extends Limit {
    def raise(stack: Decimal, bb: Decimal, potSize: Decimal) = (bb, potSize)
  }

  trait Limited

  object Limited {
    implicit def string2Limited(v: String): Limited = v match {
      case "texas"     ⇒ Texas
      case "omaha"     ⇒ Omaha
      case "omaha8"    ⇒ Omaha8
      case "stud"      ⇒ Stud
      case "stud8"     ⇒ Stud8
      case "razz"      ⇒ Razz
      case "london"    ⇒ London
      case "five-card" ⇒ FiveCard
      case "single27"  ⇒ Single27
      case "triple27"  ⇒ Triple27
      case "badugi"    ⇒ Badugi
    }
  }

  case object Texas extends Limited
  case object Omaha extends Limited
  case object Omaha8 extends Limited

  case object Stud extends Limited
  case object Stud8 extends Limited
  case object Razz extends Limited
  case object London extends Limited

  case object FiveCard extends Limited
  case object Single27 extends Limited
  case object Triple27 extends Limited
  case object Badugi extends Limited

  trait Mixed

  object Mixed {
    implicit def string2Mixed(v: String): Mixed = v match {
      case "eight" ⇒ Game.Eight
      case "horse" ⇒ Game.Horse
    }
  }

  case object Horse extends Mixed
  case object Eight extends Mixed

  trait Group
  case object Holdem extends Group
  case object SevenCard extends Group
  case object SingleDraw extends Group
  case object TripleDraw extends Group

  final val MaxTableSize = 10

  case class Options(
    val group: Group = Holdem,
    val hasBlinds: Boolean = false,
    val hasAnte: Boolean = false,
    val hasBringIn: Boolean = false,
    val hasBoard: Boolean = false,
    val hasVela: Boolean = false,

    val discards: Boolean = false,
    val reshuffle: Boolean = false,

    val maxTableSize: Int = MaxTableSize,

    val pocketSize: Int = 0,
    val streetsNum: Int = 0,
    val hiRanking: Option[Hand.Ranking] = None,
    val loRanking: Option[Hand.Ranking] = None,

    val defaultLimit: Limit = NoLimit)
}

case class Game(val game: Game.Limited, var Limit: Option[Game.Limit] = None, var TableSize: Option[Int] = None) extends Variation {
  val options = Games.Default(game)
  val tableSize: Int = TableSize match {
    case None ⇒ options.maxTableSize
    case Some(size) ⇒
      if (size > options.maxTableSize)
        options.maxTableSize
      else
        size
  }
  val limit: Game.Limit = Limit match {
    case None        ⇒ options.defaultLimit
    case Some(limit) ⇒ limit
  }
  override def toString = "%s %s" format (game, limit)
}

object Games {
  final val Default: Map[Game.Limited, Game.Options] = Map(

    Game.Texas -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      hiRanking = Some(Hand.High),
      pocketSize = 2,
      defaultLimit = Game.NoLimit),

    Game.Omaha -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      pocketSize = 4,
      hiRanking = Some(Hand.High),
      defaultLimit = Game.PotLimit),

    Game.Omaha8 -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      pocketSize = 4,
      hiRanking = Some(Hand.High),
      loRanking = Some(Hand.AceFive8),
      defaultLimit = Game.PotLimit),

    Game.Stud -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.High),
      defaultLimit = Game.FixedLimit),

    Game.Stud8 -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.High),
      loRanking = Some(Hand.AceFive8),
      defaultLimit = Game.FixedLimit),

    Game.Razz -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.AceFive),
      defaultLimit = Game.FixedLimit),

    Game.London -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.AceSix),
      defaultLimit = Game.FixedLimit),

    Game.FiveCard -> Game.Options(
      group = Game.SingleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 1,
      hiRanking = Some(Hand.High),
      defaultLimit = Game.FixedLimit),

    Game.Single27 -> Game.Options(
      group = Game.SingleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 1,
      hiRanking = Some(Hand.DeuceSeven),
      defaultLimit = Game.FixedLimit),

    Game.Triple27 -> Game.Options(
      group = Game.TripleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 3,
      hiRanking = Some(Hand.DeuceSeven),
      defaultLimit = Game.FixedLimit),

    Game.Badugi -> Game.Options(
      group = Game.TripleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 4,
      hiRanking = Some(Hand.Badugi),
      defaultLimit = Game.FixedLimit))
}
