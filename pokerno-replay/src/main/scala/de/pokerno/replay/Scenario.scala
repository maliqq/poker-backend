package de.pokerno.replay

import de.pokerno.format.text.Lexer.{ Token, BettingSemantic, Tags ⇒ tags }
import de.pokerno.poker.Card
import de.pokerno.protocol._
import de.pokerno.protocol.Conversions._
import de.pokerno.protocol.wire.Conversions._
import de.pokerno.gameplay.Street
import wire.Conversions._
import proto.wire.{BetSchema, DealType, VariationSchema}
import proto.rpc._

import de.pokerno.format.text

private[replay] object Scenario {
  def parse(src: scala.io.Source) = {
    val scenario = new Scenario()

    text.Parser.parse(src).foreach {
      case (line, lineno, tag) ⇒
        scenario.process(tag)
    }

    scenario
  }
}

private[replay] class Scenario {

  implicit def arrayByte2byteString(a: Array[Byte]) = com.dyuproject.protostuff.ByteString.copyFrom(a)

  var table: Option[wire.Table] = None
  var stake: Option[wire.Stake] = None
  var variation: Option[wire.Variation] = None
  var id: Option[String] = None
  var speed: Int = 1
  var deck: Option[Array[Byte]] = None
  val streets = new java.util.ArrayList[String]()

  def currentStreet = {
    if (streets.isEmpty) throw ReplayError("street not started yet")
    streets.get(streets.size - 1)
  }

  val actions = new java.util.HashMap[String, java.util.ArrayList[cmd.Cmd]]()

  var showdown: Boolean = false

  var processor: Function1[Token, Unit] = processMain

  def process(t: Token) = processor(t)

  var paused = false

  def processMain(t: Token): Unit =
    if (!paused) t match {
      case tags.Table(_id, size) ⇒
        val t = new wire.Table(
          size,
          seats = new java.util.ArrayList[wire.Seat]()
        )

        (0 until size) foreach { i ⇒
          t.seats.add(null)
        }

        table = Some(t)
        id = Some(_id.unquote)
        processor = processTable

      case tags.Speed(duration) ⇒
        if (duration >= 0 && duration <= 10)
          speed = duration

      case tags.Street(name) ⇒
        streets.add(name)
        actions.put(name, new java.util.ArrayList[cmd.Cmd]())
        processor = processStreet

      case tags.Showdown() ⇒
        showdown = true

      case tags.Deck(cards) ⇒
        deck = Some(cards)

      case tags.Pause() =>
        paused = true

      case x ⇒
        Console printf ("UNHANDLED: %s\n", x)
    }

  import collection.JavaConversions._

  def bet(player: String, bet: wire.Bet): Unit = {
    val t = table.getOrElse(throw ReplayError("betting before TABLE"))

    for (seat ← t.seats) {
      if (seat.player == player) {
        actions.get(currentStreet).add(cmd.AddBet(seat.player, bet))
      }
    }

  }

  def processStreet(tok: Token) = tok match {
    case tags.Ante(player) ⇒
      bet(player.unquote,
        wire.Bet(BetSchema.BetType.ANTE, stake.get.ante))

    case _: BettingSemantic ⇒
      val t = table.getOrElse(throw ReplayError("betting before TABLE"))
      val s = stake.getOrElse(throw ReplayError("STAKE is required"))
      tok match {
        case _: tags.Antes ⇒
          for (seat ← t.seats) {
            if (seat.player != null) {
              val ante = wire.Bet(BetSchema.BetType.ANTE, s.ante)
              actions.get(currentStreet).add(cmd.AddBet(seat.player, ante))
            }
          }

        case tags.Sb(player) ⇒
          bet(player.unquote,
            wire.Bet(BetSchema.BetType.SB, s.smallBlind))

        case tags.Bb(player) ⇒
          val s = stake.getOrElse(throw ReplayError("STAKE is required"))
          bet(player.unquote,
            wire.Bet(BetSchema.BetType.BB, s.bigBlind))

        case tags.Raise(player, amount) ⇒
          bet(player.unquote,
            wire.Bet(BetSchema.BetType.RAISE, amount))

        case tags.AllIn(player) ⇒
          bet(player.unquote,
            wire.Bet(BetSchema.BetType.ALLIN))

        case tags.Call(player, amount) ⇒
          bet(player.unquote,
            wire.Bet(BetSchema.BetType.CALL, amount))

        case tags.Check(player) ⇒
          bet(player.unquote,
            wire.Bet(BetSchema.BetType.CHECK))

        case tags.Fold(player) ⇒
          bet(player.unquote,
            wire.Bet(BetSchema.BetType.FOLD))
      }

    case tags.Deal(player, cards, cardsNum) ⇒

      val action = if (player != null)
        cmd.DealCards(DealType.HOLE, player.unquote, cards, cardsNum)
      else
        cmd.DealCards(DealType.BOARD, null, cards, null)

      actions.get(currentStreet).add(action)

    case s @ tags.Street(name) ⇒
      processor = processMain
      process(s)

    case x ⇒
      processor = processMain
      process(x)
  }

  def processTable(t: Token) = t match {
    case tags.Seat(pos, name, stack) ⇒
      val t = table.getOrElse(throw ReplayError("SEAT is declared before TABLE"))
      val player = name.unquote
      t.seats(pos) = new wire.Seat(null, player, stack)

    case tags.Stake(sb, bb, ante) ⇒
      val s = wire.Stake(bb, sb, if (ante.isDefined) ante.get else null)
      stake = Some(s)

    case tags.Game(game, limit) ⇒
      val t = table.getOrElse(throw ReplayError("GAME is declared before TABLE"))
      variation = Some(
        new wire.Variation(
          VariationSchema.VariationType.GAME,
          game = new wire.Game(game, limit, t.size)
        )
      )

    case tags.Button(pos) ⇒
      val t = table.getOrElse(throw ReplayError("BUTTON is declared before TABLE"))
      t.button = pos

    case x: Any ⇒
      processor = processMain
      process(x)
  }

}
