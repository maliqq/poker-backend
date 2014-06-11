package de.pokerno

import math.{BigDecimal => Decimal}

package object model {
  type Player = String
  
  final val Rates: Map[Bet.ForcedType, Decimal] = Map(
    Bet.Ante -> 0.1,
    Bet.BringIn -> 0.25,
    Bet.SmallBlind -> 0.5,
    Bet.BigBlind -> 1.0)
  
  import de.pokerno.poker.Hand
  
  final val Games: Map[GameType, Game.Options] = Map(
    GameType.Texas -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      hiRanking = Some(Hand.High),
      pocketSize = 2,
      defaultLimit = Limit.None),

    GameType.Omaha -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      pocketSize = 4,
      hiRanking = Some(Hand.High),
      defaultLimit = Limit.Pot),

    GameType.Omaha8 -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      pocketSize = 4,
      hiRanking = Some(Hand.High),
      loRanking = Some(Hand.AceFive8),
      defaultLimit = Limit.Pot),

    GameType.Stud -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.High),
      defaultLimit = Limit.Fixed),

    GameType.Stud8 -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.High),
      loRanking = Some(Hand.AceFive8),
      defaultLimit = Limit.Fixed),

    GameType.Razz -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.AceFive),
      defaultLimit = Limit.Fixed),

    GameType.London -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.AceSix),
      defaultLimit = Limit.Fixed),

    GameType.FiveCard -> Game.Options(
      group = Game.SingleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 1,
      hiRanking = Some(Hand.High),
      defaultLimit = Limit.Fixed),

    GameType.Single27 -> Game.Options(
      group = Game.SingleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 1,
      hiRanking = Some(Hand.DeuceSeven),
      defaultLimit = Limit.Fixed),

    GameType.Triple27 -> Game.Options(
      group = Game.TripleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 3,
      hiRanking = Some(Hand.DeuceSeven),
      defaultLimit = Limit.Fixed),

    GameType.Badugi -> Game.Options(
      group = Game.TripleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 4,
      streetsNum = 3,
      hiRanking = Some(Hand.Badugi),
      defaultLimit = Limit.Fixed))

  final val Mixes: Map[MixType, List[Tuple2[GameType, Limit]]] = Map(
    MixType.Horse -> List(GameType.Texas, GameType.Omaha8, GameType.Razz, GameType.Stud, GameType.Stud8).map((_, Limit.Fixed)),
    MixType.Eight -> List(
      (GameType.Triple27, Limit.Fixed),
      (GameType.Texas, Limit.Fixed),
      (GameType.Omaha8, Limit.Fixed),
      (GameType.Razz, Limit.Fixed),
      (GameType.Stud, Limit.Fixed),
      (GameType.Stud8, Limit.Fixed),
      (GameType.Texas, Limit.None),
      (GameType.Omaha, Limit.Pot)))
}
