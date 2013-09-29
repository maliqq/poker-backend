package pokerno.backend.model

import pokerno.backend.poker.Hand

trait Variation {
  def isMixed: Boolean = this.isInstanceOf[Mix]
}

object Game {
  trait Limit
  
  case object NoLimit extends Limit
  case object FixedLimit extends Limit
  case object PotLimit extends Limit

  trait Limited
  
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
  
  case object Horse extends Mixed
  case object Eight extends Mixed
  
  trait Group
  case object Holdem extends Group
  case object SevenCard extends Group
  case object SingleDraw extends Group
  case object TripleDraw extends Group
  
  final val MaxTableSize = 10
  
  class Options(
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
    
    val defaultLimit: Limit = NoLimit
  )
}

class Game(val game: Game.Limited, var _limit: Option[Game.Limit] = None, var _tableSize: Option[Int] = None) extends Variation {
  val options = Games.Default(game)
  val tableSize: Int = _tableSize match {
    case None => Game.MaxTableSize
    case Some(size) =>
      if (size > Game.MaxTableSize)
        Game.MaxTableSize
      else
        size
  }
  val limit: Game.Limit = _limit match {
    case None => options.defaultLimit
    case Some(limit) => limit
  }
  override def toString = "%s %s".format(game, limit)
}

object Games {
  final val Default: Map[Game.Limited, Game.Options] = Map(
  
  Game.Texas -> new Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      hiRanking = Some(Hand.High),
      pocketSize = 2,
      defaultLimit = Game.NoLimit
  ),

  Game.Omaha -> new Game.Options(
    group =        Game.Holdem,
    hasBoard =     true,
    hasBlinds =    true,
    maxTableSize = 10,
    pocketSize =   4,
    hiRanking = Some(Hand.High),
    defaultLimit = Game.PotLimit
  ),

  Game.Omaha8 -> new Game.Options(
    group =        Game.Holdem,
    hasBoard =     true,
    hasBlinds =    true,
    maxTableSize = 10,
    pocketSize =   4,
    hiRanking = Some(Hand.High),
    loRanking = Some(Hand.AceFive8),
    defaultLimit = Game.PotLimit
  ),

  Game.Stud -> new Game.Options(
    group =        Game.SevenCard,
    hasAnte =      true,
    hasBringIn =   true,
    hasVela =      true,
    maxTableSize = 8,
    pocketSize =   7,
    hiRanking = Some(Hand.High),
    defaultLimit = Game.FixedLimit
  ),

  Game.Stud8 -> new Game.Options(
    group =        Game.SevenCard,
    hasAnte =      true,
    hasBringIn =   true,
    hasVela =      true,
    maxTableSize = 8,
    pocketSize =   7,
    hiRanking = Some(Hand.High),
    loRanking = Some(Hand.AceFive8),
    defaultLimit = Game.FixedLimit
  ),

  Game.Razz -> new Game.Options(
    group =        Game.SevenCard,
    hasAnte =      true,
    hasBringIn =   true,
    hasVela =      true,
    maxTableSize = 8,
    pocketSize =   7,
    hiRanking = Some(Hand.AceFive),
    defaultLimit = Game.FixedLimit
  ),

  Game.London -> new Game.Options(
    group =        Game.SevenCard,
    hasAnte =      true,
    hasBringIn =   true,
    hasVela =      true,
    maxTableSize = 8,
    pocketSize =   7,
    hiRanking = Some(Hand.AceSix),
    defaultLimit = Game.FixedLimit
  ),

  Game.FiveCard -> new Game.Options(
    group =        Game.SingleDraw,
    hasBlinds =    true,
    discards =     true,
    reshuffle =    true,
    maxTableSize = 6,
    pocketSize =   5,
    streetsNum =      1,
    hiRanking = Some(Hand.High),
    defaultLimit = Game.FixedLimit
  ),

  Game.Single27 -> new Game.Options(
    group =        Game.SingleDraw,
    hasBlinds =    true,
    discards =     true,
    reshuffle =    true,
    maxTableSize = 6,
    pocketSize =   5,
    streetsNum =      1,
    hiRanking = Some(Hand.DeuceSeven),
    defaultLimit = Game.FixedLimit
  ),

  Game.Triple27 -> new Game.Options(
    group =        Game.TripleDraw,
    hasBlinds =    true,
    discards =     true,
    reshuffle =    true,
    maxTableSize = 6,
    pocketSize =   5,
    streetsNum =      3,
    hiRanking = Some(Hand.DeuceSeven),
    defaultLimit = Game.FixedLimit
  ),

  Game.Badugi -> new Game.Options(
    group =        Game.TripleDraw,
    hasBlinds =    true,
    discards =     true,
    reshuffle =    true,
    maxTableSize = 6,
    pocketSize =   4,
    hiRanking = Some(Hand.Badugi),
    defaultLimit = Game.FixedLimit
  ))
}
