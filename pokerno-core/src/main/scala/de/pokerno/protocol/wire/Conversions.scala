package de.pokerno.protocol.wire

import de.pokerno.{ model, poker, gameplay }
import proto.wire._

object Conversions {

  implicit def table2wire(table: model.Table) = {
    val t = new Table(table.size, (table.button: Int))
    val seats = new java.util.ArrayList[Seat]()
    for (seat ← (table.seats: List[model.Seat])) {
      seats.add(new Seat(
        state = seat.state,
        player = seat.player match {
          case Some(p) ⇒ p.toString
          case None    ⇒ null
        },
        stackAmount = seat.stack.toDouble,
        putAmount = seat.put.toDouble
      ))
    }
    t.seats = seats
    t
  }

  implicit def variation2wire(v: model.Variation) = v match {
    case g @ model.Game(game, limit, tableSize) ⇒
      newGame(game, g.limit, g.tableSize)

    case m @ model.Mix(game, tableSize) ⇒
      newMix(game, m.tableSize)
  }

  def newGame(game: model.Game.Limited, limit: model.Game.Limit, tableSize: Int) =
    new Variation(VariationSchema.VariationType.GAME, game = new Game(
      game, limit, tableSize
    ))

  def newMix(game: model.Game.Mixed, tableSize: Int) =
    new Variation(VariationSchema.VariationType.MIX,
      mix = new Mix(
        game, tableSize
      ))

  implicit def limit2wire(limit: model.Game.Limit): GameSchema.GameLimit = limit match {
    case model.Game.NoLimit    ⇒ GameSchema.GameLimit.NL
    case model.Game.PotLimit   ⇒ GameSchema.GameLimit.PL
    case model.Game.FixedLimit ⇒ GameSchema.GameLimit.FL
  }

  implicit def wire2limit(limit: GameSchema.GameLimit): model.Game.Limit = limit match {
    case GameSchema.GameLimit.NL ⇒ model.Game.NoLimit
    case GameSchema.GameLimit.PL ⇒ model.Game.PotLimit
    case GameSchema.GameLimit.FL ⇒ model.Game.FixedLimit
  }

  implicit def stake2wire(stake: model.Stake) = new Stake(
    bigBlind = stake.bigBlind.toDouble,
    smallBlind = stake.smallBlind.toDouble,
    ante = stake.ante match {
      case Some(n) ⇒ n.toDouble
      case None    ⇒ null
    }
  )

  implicit def dealCards2wire(v: model.DealCards.Value): DealType = v match {
    case model.DealCards.Board ⇒ DealType.BOARD
    case model.DealCards.Door  ⇒ DealType.DOOR
    case model.DealCards.Hole  ⇒ DealType.HOLE
  }

  implicit def wire2dealCards(w: DealType): model.DealCards.Value = w match {
    case DealType.BOARD ⇒ model.DealCards.Board
    case DealType.DOOR  ⇒ model.DealCards.Door
    case DealType.HOLE  ⇒ model.DealCards.Hole
  }

  implicit def limitedGame2wire(g: model.Game.Limited): GameSchema.GameType = g match {
    case model.Game.Texas    ⇒ GameSchema.GameType.TEXAS
    case model.Game.Omaha    ⇒ GameSchema.GameType.OMAHA
    case model.Game.Omaha8   ⇒ GameSchema.GameType.OMAHA_8
    case model.Game.Stud     ⇒ GameSchema.GameType.STUD
    case model.Game.Stud8    ⇒ GameSchema.GameType.STUD_8
    case model.Game.Razz     ⇒ GameSchema.GameType.RAZZ
    case model.Game.London   ⇒ GameSchema.GameType.LONDON
    case model.Game.FiveCard ⇒ GameSchema.GameType.FIVE_CARD
    case model.Game.Single27 ⇒ GameSchema.GameType.SINGLE_27
    case model.Game.Triple27 ⇒ GameSchema.GameType.TRIPLE_27
    case model.Game.Badugi   ⇒ GameSchema.GameType.BADUGI

    case _                   ⇒ throw new IllegalArgumentException(f"Unknown limited game: ${g.toString}")
  }

  implicit def wire2limitedGame(g: GameSchema.GameType): model.Game.Limited = g match {
    case GameSchema.GameType.TEXAS     ⇒ model.Game.Texas
    case GameSchema.GameType.OMAHA     ⇒ model.Game.Omaha
    case GameSchema.GameType.OMAHA_8   ⇒ model.Game.Omaha8
    case GameSchema.GameType.STUD      ⇒ model.Game.Stud
    case GameSchema.GameType.STUD_8    ⇒ model.Game.Stud8
    case GameSchema.GameType.RAZZ      ⇒ model.Game.Razz
    case GameSchema.GameType.LONDON    ⇒ model.Game.London
    case GameSchema.GameType.FIVE_CARD ⇒ model.Game.FiveCard
    case GameSchema.GameType.SINGLE_27 ⇒ model.Game.Single27
    case GameSchema.GameType.TRIPLE_27 ⇒ model.Game.Triple27
    case GameSchema.GameType.BADUGI    ⇒ model.Game.Badugi
  }

  implicit def game2wire(g: model.Game): Game = new Game(g.game, g.limit, g.tableSize)

  implicit def mixedGame2wire(m: model.Game.Mixed): MixSchema.MixType = m match {
    case model.Game.Eight ⇒ MixSchema.MixType.EIGHT_GAME
    case model.Game.Horse ⇒ MixSchema.MixType.HORSE

    case _                ⇒ throw new IllegalArgumentException(f"Unknown mixed game: ${m.toString}")
  }

  implicit def mix2wire(m: model.Mix): Mix = new Mix(m.game, m.tableSize)

  implicit def betType2wire(v: model.Bet.Value): BetSchema.BetType = v match {
    case model.Bet.Call       ⇒ BetSchema.BetType.CALL
    case model.Bet.Raise      ⇒ BetSchema.BetType.RAISE
    case model.Bet.Check      ⇒ BetSchema.BetType.CHECK
    case model.Bet.Fold       ⇒ BetSchema.BetType.FOLD
    case model.Bet.Ante       ⇒ BetSchema.BetType.ANTE
    case model.Bet.BringIn    ⇒ BetSchema.BetType.BRING_IN
    case model.Bet.SmallBlind ⇒ BetSchema.BetType.SB
    case model.Bet.BigBlind   ⇒ BetSchema.BetType.BB
    case model.Bet.GuestBlind ⇒ BetSchema.BetType.GUEST_BLIND
    case model.Bet.Straddle   ⇒ BetSchema.BetType.STRADDLE
  }

  implicit def wire2betType(v: BetSchema.BetType): model.Bet.Value = v match {
    case BetSchema.BetType.CALL        ⇒ model.Bet.Call
    case BetSchema.BetType.RAISE       ⇒ model.Bet.Raise
    case BetSchema.BetType.CHECK       ⇒ model.Bet.Check
    case BetSchema.BetType.FOLD        ⇒ model.Bet.Fold
    case BetSchema.BetType.ANTE        ⇒ model.Bet.Ante
    case BetSchema.BetType.BRING_IN    ⇒ model.Bet.BringIn
    case BetSchema.BetType.SB          ⇒ model.Bet.SmallBlind
    case BetSchema.BetType.BB          ⇒ model.Bet.BigBlind
    case BetSchema.BetType.GUEST_BLIND ⇒ model.Bet.GuestBlind
    case BetSchema.BetType.STRADDLE    ⇒ model.Bet.Straddle
    case BetSchema.BetType.ALLIN       ⇒ model.Bet.AllIn
  }

  implicit def seatState2wire(v: model.Seat.State): SeatSchema.SeatState = v match {
    case model.Seat.State.Empty  ⇒ SeatSchema.SeatState.EMPTY
    case model.Seat.State.Taken  ⇒ SeatSchema.SeatState.TAKEN
    case model.Seat.State.Ready  ⇒ SeatSchema.SeatState.READY

    case model.Seat.State.WaitBB ⇒ SeatSchema.SeatState.POST_BB
    case model.Seat.State.PostBB ⇒ SeatSchema.SeatState.WAIT_BB

    case model.Seat.State.Play   ⇒ SeatSchema.SeatState.PLAY
    case model.Seat.State.AllIn  ⇒ SeatSchema.SeatState.ALL_IN
    case model.Seat.State.Bet    ⇒ SeatSchema.SeatState.BET
    case model.Seat.State.Fold   ⇒ SeatSchema.SeatState.FOLD
    case model.Seat.State.Auto   ⇒ SeatSchema.SeatState.AUTO

    case model.Seat.State.Idle   ⇒ SeatSchema.SeatState.IDLE
    case model.Seat.State.Away   ⇒ SeatSchema.SeatState.AWAY
  }

  implicit def wire2state(v: SeatSchema.SeatState): model.Seat.State = v match {
    case SeatSchema.SeatState.EMPTY   ⇒ model.Seat.State.Empty
    case SeatSchema.SeatState.TAKEN   ⇒ model.Seat.State.Taken
    case SeatSchema.SeatState.READY   ⇒ model.Seat.State.Ready

    case SeatSchema.SeatState.POST_BB ⇒ model.Seat.State.WaitBB
    case SeatSchema.SeatState.WAIT_BB ⇒ model.Seat.State.PostBB

    case SeatSchema.SeatState.PLAY    ⇒ model.Seat.State.Play
    case SeatSchema.SeatState.ALL_IN  ⇒ model.Seat.State.AllIn
    case SeatSchema.SeatState.BET     ⇒ model.Seat.State.Bet
    case SeatSchema.SeatState.FOLD    ⇒ model.Seat.State.Fold
    case SeatSchema.SeatState.AUTO    ⇒ model.Seat.State.Auto

    case SeatSchema.SeatState.IDLE    ⇒ model.Seat.State.Idle
    case SeatSchema.SeatState.AWAY    ⇒ model.Seat.State.Away
  }

  //  import com.dyuproject.protostuff.ByteString
  //  implicit def byteString2Cards(v: ByteString): List[poker.Card] = v.toByteArray.map(poker.Card.wrap(_)).toList
  //  implicit def cards2ByteString(v: List[poker.Card]): ByteString = ByteString.copyFrom(v.map(_.toByte).toArray)
  //  
  implicit def rank2wire(v: poker.Rank): HandSchema.RankType = v match {

    case poker.Rank.Badugi.BadugiOne   ⇒ HandSchema.RankType.BADUGI1
    case poker.Rank.Badugi.BadugiTwo   ⇒ HandSchema.RankType.BADUGI2
    case poker.Rank.Badugi.BadugiThree ⇒ HandSchema.RankType.BADUGI3
    case poker.Rank.Badugi.BadugiFour  ⇒ HandSchema.RankType.BADUGI4

    case poker.Rank.High.StraightFlush ⇒ HandSchema.RankType.STRAIGHT_FLUSH
    case poker.Rank.High.FourKind      ⇒ HandSchema.RankType.FOUR_KIND
    case poker.Rank.High.FullHouse     ⇒ HandSchema.RankType.FULL_HOUSE
    case poker.Rank.High.Flush         ⇒ HandSchema.RankType.FLUSH
    case poker.Rank.High.Straight      ⇒ HandSchema.RankType.STRAIGHT
    case poker.Rank.High.ThreeKind     ⇒ HandSchema.RankType.THREE_KIND
    case poker.Rank.High.TwoPair       ⇒ HandSchema.RankType.TWO_PAIR
    case poker.Rank.High.OnePair       ⇒ HandSchema.RankType.ONE_PAIR
    case poker.Rank.High.HighCard      ⇒ HandSchema.RankType.HIGH_CARD

    case poker.Rank.Low.Complete       ⇒ HandSchema.RankType.LOW
    case poker.Rank.Low.Incomplete     ⇒ HandSchema.RankType.NOT_LOW

  }

  implicit def wire2rank(v: HandSchema.RankType): poker.Rank = v match {
    case HandSchema.RankType.BADUGI1        ⇒ poker.Rank.Badugi.BadugiOne
    case HandSchema.RankType.BADUGI2        ⇒ poker.Rank.Badugi.BadugiTwo
    case HandSchema.RankType.BADUGI3        ⇒ poker.Rank.Badugi.BadugiThree
    case HandSchema.RankType.BADUGI4        ⇒ poker.Rank.Badugi.BadugiFour

    case HandSchema.RankType.STRAIGHT_FLUSH ⇒ poker.Rank.High.StraightFlush
    case HandSchema.RankType.FOUR_KIND      ⇒ poker.Rank.High.FourKind
    case HandSchema.RankType.FULL_HOUSE     ⇒ poker.Rank.High.FullHouse
    case HandSchema.RankType.FLUSH          ⇒ poker.Rank.High.Flush
    case HandSchema.RankType.STRAIGHT       ⇒ poker.Rank.High.Straight
    case HandSchema.RankType.THREE_KIND     ⇒ poker.Rank.High.ThreeKind
    case HandSchema.RankType.TWO_PAIR       ⇒ poker.Rank.High.TwoPair
    case HandSchema.RankType.ONE_PAIR       ⇒ poker.Rank.High.OnePair
    case HandSchema.RankType.HIGH_CARD      ⇒ poker.Rank.High.HighCard

    case HandSchema.RankType.LOW            ⇒ poker.Rank.Low.Complete
    case HandSchema.RankType.NOT_LOW        ⇒ poker.Rank.Low.Incomplete // FIXME
  }

  implicit def range2wire(r: model.Range) = new Range(r.min.toDouble, r.max.toDouble)

  implicit def wire2range(w: Range) = new model.Range((w.min.toDouble, w.max.toDouble))

}
