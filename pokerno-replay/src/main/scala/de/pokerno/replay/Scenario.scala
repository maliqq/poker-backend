package de.pokerno.replay

import de.pokerno.format.text.Lexer.{ Token, BettingSemantic, Tags ⇒ tags }
import de.pokerno.model._
import de.pokerno.poker.{ Card, Cards}
import de.pokerno.protocol.cmd

import de.pokerno.format.text

private[replay] object Scenario {
  def parse(id: String, src: scala.io.Source) = {
    val scenario = new Scenario(id)

    text.Parser.parse(src).foreach {
      case (line, lineno, tag) ⇒
        scenario.process(tag)
    }

    scenario
  }
}

private[replay] class Scenario(val name: String) {

  var table: Option[Table] = None
  var stake: Option[Stake] = None
  var variation: Option[Variation] = None
  var id: Option[String] = None
  var speed: Int = 1
  var deck: Option[Array[Byte]] = None
  val streets = collection.mutable.ListBuffer[String]()

  def currentStreet = {
    if (streets.isEmpty) throw ReplayError("street not started yet")
    streets(streets.size - 1)
  }

  val actions = collection.mutable.Map[String, collection.mutable.ListBuffer[de.pokerno.protocol.Command]]()

  var showdown: Boolean = false

  var processor: Function1[Token, Unit] = processMain

  def process(t: Token) = processor(t)

  var paused = false

  def processMain(t: Token): Unit =
    if (!paused) t match {
      case tags.Table(_id, size) ⇒
        val t = new Table(size)

        table = Some(t)
        id = Some(_id.unquote)
        processor = processTable

      case tags.Speed(duration) ⇒
        if (duration >= 0 && duration <= 10)
          speed = duration

      case tags.Street(name) ⇒
        streets += name
        actions(name) = collection.mutable.ListBuffer[de.pokerno.protocol.Command]()
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

  def bet(player: Player, bet: Bet): Unit = {
    val t = table.getOrElse(throw ReplayError("betting before TABLE"))

    for (seat ← t.seats) {
      if (seat.player.orNull == player) {
        actions(currentStreet) += cmd.AddBet(seat.player.get, bet)
      }
    }

  }
  
  def action(a: de.pokerno.protocol.Command) = {
    actions(currentStreet) += a
  }

  def processStreet(tok: Token) = tok match {
    case tags.Ante(player) ⇒
      bet(player.unquote, Bet.Ante(stake.get.ante.get))

    case _: BettingSemantic ⇒
      val t = table.getOrElse(throw ReplayError("betting before TABLE"))
      val s = stake.getOrElse(throw ReplayError("STAKE is required"))
      tok match {
        case _: tags.Antes ⇒
          for (seat ← t.seats) {
            if (seat.player != null) {
              val ante = Bet.Ante(s.ante.get)
              this action cmd.AddBet(seat.player.get, ante)
            }
          }

        case tags.Sb(player) ⇒
          bet(player.unquote, Bet.SmallBlind(s.smallBlind))

        case tags.Bb(player) ⇒
          val s = stake.getOrElse(throw ReplayError("STAKE is required"))
          bet(player.unquote, Bet.BigBlind(s.bigBlind))

        case tags.Raise(player, amount) ⇒
          bet(player.unquote, Bet.Raise(amount))

        case tags.AllIn(player) ⇒
          bet(player.unquote, Bet.AllIn)

        case tags.Call(player, amount) ⇒
          bet(player.unquote, Bet.call(if (amount.isDefined) amount.get else null))

        case tags.Check(player) ⇒
          bet(player.unquote, Bet.check)

        case tags.Fold(player) ⇒
          bet(player.unquote, Bet.fold)
      }

    case tags.Deal(player, _cards, cardsNum) ⇒
      val cards = Cards.fromString(_cards)
      
      this action (if (player != null)
        cmd.DealCards(DealType.Hole, if (cards != null) Left(cards) else Right(Some(cardsNum)), Some(player.unquote))
      else
        cmd.DealCards(DealType.Board, Left(cards))
      )

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
      t.takeSeat(pos, player, Some(stack))

    case tags.Stake(sb, bb, ante) ⇒
      stake = Some(
          Stake(bb,
              Some(sb),
              if (ante.isDefined) Left(ante.get) else Right(false)
              )
        )

    case tags.Game(game, limit) ⇒
      val t = table.getOrElse(throw ReplayError("GAME is declared before TABLE"))
      val g: Game.Limited = game 
      if (g == null) throw ReplayError("unknown game")
      val l: Game.Limit = limit
      variation = Some(new Game(g, Option(l), Some(t.size)))

    case tags.Button(pos) ⇒
      val t = table.getOrElse(throw ReplayError("BUTTON is declared before TABLE"))
      t.button.current = pos

    case x: Any ⇒
      processor = processMain
      process(x)
  }

}
