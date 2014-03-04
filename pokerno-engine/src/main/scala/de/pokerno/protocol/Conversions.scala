package de.pokerno.protocol

import math.{ BigDecimal ⇒ Decimal }

import de.pokerno.{ model, poker, gameplay }

import com.dyuproject.protostuff.ByteString

object CommonConversions {
  implicit def decimal2wire(d: Decimal): java.lang.Double = d.toDouble

  implicit def wire2decimal(w: java.lang.Double): Decimal = Decimal.double2bigDecimal(w)
  
  implicit def player2wire(p: model.Player): String = {
    if (p != null) return p.id
    return null
  }

  implicit def wire2player(w: String): model.Player =
    new model.Player(w)

  implicit def range2wire(r: model.Range) = new wire.Range(r.min.toDouble, r.max.toDouble)

  implicit def wire2range(w: Range) = new model.Range((w.min.toDouble, w.max.toDouble))

  implicit def cards2wire(c: List[poker.Card]): ByteString =
    ByteString.copyFrom(c.map(_.toByte).toArray)

  implicit def wire2cards(w: ByteString): List[poker.Card] =
    w.toByteArray.map(poker.Card.wrap(_)).toList

}

object Conversions {

  import CommonConversions._
  import WireConversions._
  import MsgConversions._
  //import RpcConversions._
  
  implicit def bet2wire(b: model.Bet): wire.Bet = {
    if (b != null) return wire.Bet(b.betType, b.amount match {
      case Some(n) ⇒ n.toDouble
      case None    ⇒ null
    }, b.timeout match {
      case Some(flag) ⇒ flag
      case None       ⇒ null
    })
    return null
  }

  implicit def wire2bet(w: wire.Bet) =
    new model.Bet(w.getType, w.getAmount match {
      case null ⇒ None
      case n    ⇒ Some(n)
    })

  implicit def hand2wire(h: poker.Hand) = new wire.Hand(
    cards = h.cards.value,
    rank = h.rank.get,
    high = h.high,
    value = h.value,
    kicker = h.kicker,
    string = h.description
  )

  import poker.Hand._

  implicit def wire2hand(w: wire.Hand) = new poker.Hand(wire2cards(w.cards),
    rank = Some(w.rank),
    value = w.value,
    High = Left(w.high),
    Kicker = Left(w.kicker)
  )

}

object MsgConversions {
  
  import CommonConversions._
  import de.pokerno.protocol.msg._
  import proto.wire.StreetType

  implicit def street2wire(s: gameplay.Street.Value): StreetType = s match {
    //case null => null
    case gameplay.Street.Preflop    ⇒ StreetType.PREFLOP
    case gameplay.Street.Flop       ⇒ StreetType.FLOP
    case gameplay.Street.Turn       ⇒ StreetType.TURN
    case gameplay.Street.River      ⇒ StreetType.RIVER

    case gameplay.Street.Third      ⇒ StreetType.THIRD
    case gameplay.Street.Fourth     ⇒ StreetType.FOURTH
    case gameplay.Street.Fifth      ⇒ StreetType.FIFTH
    case gameplay.Street.Sixth      ⇒ StreetType.SIXTH
    case gameplay.Street.Seventh    ⇒ StreetType.SEVENTH

    case gameplay.Street.Predraw    ⇒ StreetType.PREDRAW
    case gameplay.Street.Draw       ⇒ StreetType.DRAW
    case gameplay.Street.FirstDraw  ⇒ StreetType.FIRST_DRAW
    case gameplay.Street.SecondDraw ⇒ StreetType.SECOND_DRAW
    case gameplay.Street.ThirdDraw  ⇒ StreetType.THIRD_DRAW
  }

  implicit def play2wire(v: gameplay.Play): Play = {
    if (v != null) {
      val play = new Play(v.id, v.startAt.getTime() / 1000)
      play.pot = v.pot.total
      v.street.map { street =>
        play.street = street
      }
      v.acting.map { a =>
        play.acting = RequireBet(pos = a._2, player = a._1, call = v.require._1, raise = v.require._2)
      }
      play.board = v.board
      play
    } else null
  }

}


object RpcConversions {

  import CommonConversions._
  import WireConversions._
  import proto.rpc._
  import de.pokerno.protocol.rpc._
  import collection.JavaConversions._
  import proto.wire.VariationSchema

  implicit def createTable(_table: wire.Table): model.Table = {
    val table = new model.Table(_table.size)
    var pos = 0
    for (seat ← _table.seats) {
      if (seat != null && seat.player != null && seat.stackAmount != null)
        table.addPlayer(pos, seat.player, Some(seat.stackAmount))
      pos += 1
    }
    table.button.current = _table.button
    table
  }

  implicit def createVariation(_variation: wire.Variation): model.Variation =
    _variation.`type` match {
      case VariationSchema.VariationType.GAME ⇒
        new model.Game(
          _variation.game.`type`,
          Some(_variation.game.limit),
          _variation.game.tableSize match {
            case null ⇒ None
            case n    ⇒ Some(n)
          }
        )
      case VariationSchema.VariationType.MIX ⇒
        // TODO
        null
    }

  implicit def createStake(_stake: wire.Stake): model.Stake =
    new model.Stake(_stake.bigBlind,
      _stake.smallBlind match {
        case null ⇒ None
        case n    ⇒ Some(n)
      },
      _stake.ante match {
        case null ⇒ Right(false)
        case n    ⇒ Left(n)
      }
    )

}

object WireConversions {
  import de.pokerno.protocol.wire._
  import proto.wire._

  implicit def table2wire(table: model.Table) = {
    val t = new Table(table.size, (table.button: Int))
    val seats = new java.util.ArrayList[Seat]()
    for (seat ← (table.seats: List[model.Seat])) {
      seats.add(new Seat(
        state = seat.state,
        presence = seat.presence match {
          case Some(v) => v
          case None => null
        },
        player = seat.player match {
          case Some(p) ⇒ p.toString
          case None    ⇒ null
        },
        stackAmount = seat.stack.toDouble,
        lastAction = if (seat.lastAction == null) null else seat.lastAction,
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

  implicit def betType2wire(v: model.Bet.Value): BetType = v match {
    case model.Bet.Call       ⇒ BetType.CALL
    case model.Bet.Raise      ⇒ BetType.RAISE
    case model.Bet.Check      ⇒ BetType.CHECK
    case model.Bet.Fold       ⇒ BetType.FOLD
    case model.Bet.Ante       ⇒ BetType.ANTE
    case model.Bet.BringIn    ⇒ BetType.BRING_IN
    case model.Bet.SmallBlind ⇒ BetType.SB
    case model.Bet.BigBlind   ⇒ BetType.BB
    case model.Bet.GuestBlind ⇒ BetType.GUEST_BLIND
    case model.Bet.Straddle   ⇒ BetType.STRADDLE
  }

  implicit def wire2betType(v: BetType): model.Bet.Value = v match {
    case BetType.CALL        ⇒ model.Bet.Call
    case BetType.RAISE       ⇒ model.Bet.Raise
    case BetType.CHECK       ⇒ model.Bet.Check
    case BetType.FOLD        ⇒ model.Bet.Fold
    case BetType.ANTE        ⇒ model.Bet.Ante
    case BetType.BRING_IN    ⇒ model.Bet.BringIn
    case BetType.SB          ⇒ model.Bet.SmallBlind
    case BetType.BB          ⇒ model.Bet.BigBlind
    case BetType.GUEST_BLIND ⇒ model.Bet.GuestBlind
    case BetType.STRADDLE    ⇒ model.Bet.Straddle
    case BetType.ALL_IN      ⇒ model.Bet.AllIn
  }

  implicit def seatState2wire(v: model.Seat.State.Value): SeatSchema.SeatState = v match {
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

  implicit def wire2state(v: SeatSchema.SeatState): model.Seat.State.Value = v match {
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
  
  implicit def presence2wire(v: model.Seat.Presence.Value): SeatSchema.PresenceType = v match {
    case model.Seat.Presence.Offline => SeatSchema.PresenceType.OFFLINE
    case model.Seat.Presence.Online => SeatSchema.PresenceType.ONLINE
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

}
