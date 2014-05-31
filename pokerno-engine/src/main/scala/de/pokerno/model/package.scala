package de.pokerno

import math.{BigDecimal => Decimal}

package object model {
  type Player = String
  
  final val Rates: Map[Bet.Value, Decimal] = Map(
    Bet.Ante -> 0.1,
    Bet.BringIn -> 0.25,
    Bet.SmallBlind -> 0.5,
    Bet.BigBlind -> 1.0)
  
  import de.pokerno.poker.Hand
  
  final val Games: Map[Game.Limited, Game.Options] = Map(
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
      streetsNum = 3,
      hiRanking = Some(Hand.Badugi),
      defaultLimit = Game.FixedLimit))

  final val Mixes: Map[Game.Mixed, List[Tuple2[Game.Limited, Game.Limit]]] = Map(
    Game.Horse -> List(Game.Texas, Game.Omaha8, Game.Razz, Game.Stud, Game.Stud8).map((_, Game.FixedLimit)),
    Game.Eight -> List(
      (Game.Triple27, Game.FixedLimit),
      (Game.Texas, Game.FixedLimit),
      (Game.Omaha8, Game.FixedLimit),
      (Game.Razz, Game.FixedLimit),
      (Game.Stud, Game.FixedLimit),
      (Game.Stud8, Game.FixedLimit),
      (Game.Texas, Game.NoLimit),
      (Game.Omaha, Game.PotLimit)))
}
