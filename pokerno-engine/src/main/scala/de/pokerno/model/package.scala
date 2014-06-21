package de.pokerno

package object model {
  type Player = String
  type Decimal = math.BigDecimal
  
  final val Rates: Map[BetType.Forced, Decimal] = Map(
    BetType.Ante -> 0.1,
    BetType.BringIn -> 0.25,
    BetType.SmallBlind -> 0.5,
    BetType.BigBlind -> 1.0)
  
  import de.pokerno.poker.Hand
  
  final val Games: Map[GameType, Game.Options] = Map(
    GameType.Texas -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      hiRanking = Some(Hand.High),
      pocketSize = 2,
      defaultLimit = GameLimit.None),

    GameType.Omaha -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      pocketSize = 4,
      hiRanking = Some(Hand.High),
      defaultLimit = GameLimit.Pot),

    GameType.Omaha8 -> Game.Options(
      group = Game.Holdem,
      hasBoard = true,
      hasBlinds = true,
      maxTableSize = 10,
      pocketSize = 4,
      hiRanking = Some(Hand.High),
      loRanking = Some(Hand.AceFive8),
      defaultLimit = GameLimit.Pot),

    GameType.Stud -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.High),
      defaultLimit = GameLimit.Fixed),

    GameType.Stud8 -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.High),
      loRanking = Some(Hand.AceFive8),
      defaultLimit = GameLimit.Fixed),

    GameType.Razz -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.AceFive),
      defaultLimit = GameLimit.Fixed),

    GameType.London -> Game.Options(
      group = Game.SevenCard,
      hasAnte = true,
      hasBringIn = true,
      hasVela = true,
      maxTableSize = 8,
      pocketSize = 7,
      hiRanking = Some(Hand.AceSix),
      defaultLimit = GameLimit.Fixed),

    GameType.FiveCard -> Game.Options(
      group = Game.SingleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 1,
      hiRanking = Some(Hand.High),
      defaultLimit = GameLimit.Fixed),

    GameType.Single27 -> Game.Options(
      group = Game.SingleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 1,
      hiRanking = Some(Hand.DeuceSeven),
      defaultLimit = GameLimit.Fixed),

    GameType.Triple27 -> Game.Options(
      group = Game.TripleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 5,
      streetsNum = 3,
      hiRanking = Some(Hand.DeuceSeven),
      defaultLimit = GameLimit.Fixed),

    GameType.Badugi -> Game.Options(
      group = Game.TripleDraw,
      hasBlinds = true,
      discards = true,
      reshuffle = true,
      maxTableSize = 6,
      pocketSize = 4,
      streetsNum = 3,
      hiRanking = Some(Hand.Badugi),
      defaultLimit = GameLimit.Fixed))

  final val Mixes: Map[MixType, List[Tuple2[GameType, GameLimit]]] = Map(
    MixType.Horse -> List(GameType.Texas, GameType.Omaha8, GameType.Razz, GameType.Stud, GameType.Stud8).map((_, GameLimit.Fixed)),
    MixType.Eight -> List(
      (GameType.Triple27, GameLimit.Fixed),
      (GameType.Texas, GameLimit.Fixed),
      (GameType.Omaha8, GameLimit.Fixed),
      (GameType.Razz, GameLimit.Fixed),
      (GameType.Stud, GameLimit.Fixed),
      (GameType.Stud8, GameLimit.Fixed),
      (GameType.Texas, GameLimit.None),
      (GameType.Omaha, GameLimit.Pot)))
}
