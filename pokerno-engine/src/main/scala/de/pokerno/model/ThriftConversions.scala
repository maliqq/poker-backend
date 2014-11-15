package de.pokerno.model

import de.pokerno.protocol.thrift

object ThriftConversions {
  
  implicit def gameTypeFromThrift(v: thrift.GameType): GameType = {
    v match {
      case thrift.GameType.Badugi => GameType.Badugi
      case thrift.GameType.FiveCard => GameType.FiveCard
      case thrift.GameType.London => GameType.London
      case thrift.GameType.Omaha => GameType.Omaha
      case thrift.GameType.Omaha8 => GameType.Omaha8
      case thrift.GameType.Razz => GameType.Razz
      case thrift.GameType.Single27 => GameType.Single27
      case thrift.GameType.Stud => GameType.Stud
      case thrift.GameType.Stud8 => GameType.Stud8
      case thrift.GameType.Texas => GameType.Texas
      case thrift.GameType.Triple27 => GameType.Triple27
    }
  }
  
  implicit def limitTypeFromThrift(v: thrift.MixType): MixType = {
    v match {
      case thrift.MixType.EightGame => MixType.Eight
      case thrift.MixType.Horse => MixType.Horse
    }
  }
  
  implicit def gameLimitFromThrift(v: thrift.GameLimit): GameLimit = {
    v match {
      case thrift.GameLimit.Fl => GameLimit.Fixed
      case thrift.GameLimit.Nl => GameLimit.None
      case thrift.GameLimit.Pl => GameLimit.Pot
    }
  }
  
  implicit def variationFromThrift(v: thrift.Variation): Variation = {
    if (v.game.isDefined) {
      val game = v.game.get
      return Game(game.`type`, Some(game.limit: GameLimit), Some(game.tableSize))
    }
    
    val mix = v.mix.get
    return Mix(mix.`type`, mix.tableSize)
  }
  
  implicit def stakeFromThrift(v: thrift.Stake): Stake = {
    Stake(
        v.bigBlind: Decimal,
        v.smallBlind.map { x => x: Decimal },
        v.buyIn,
        (v.ante match {
          case Some(v) => Left(v)
          case None => Right(false)
        }): Either[Decimal, Boolean],
        (v.bringIn match {
          case Some(v) => Left(v)
          case None => Right(false)
        }): Either[Decimal, Boolean]
      )
  }
  
}