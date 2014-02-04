package de.pokerno.replay

import de.pokerno.format.text.Lexer.{Token, BettingSemantic, Tags => tags}
import de.pokerno.model.{Player, Table, Stake, Variation, Game, Bet, Seat}
import de.pokerno.poker.Card
import de.pokerno.protocol._
import de.pokerno.protocol.Conversions._
import de.pokerno.protocol.wire.Conversions._
import de.pokerno.gameplay.Street
import wire.Conversions._

import de.pokerno.format.text

object Scenario {
  def parse(src: scala.io.Source) = {
    val scenario = new Scenario()

    text.Parser.parse(src).foreach { case (line, lineno, tag) =>
      scenario.process(tag)
    }
    
    scenario
  }
}

class Scenario {
  var table: Option[Table] = None
  var stake: Option[Stake] = None
  var variation: Option[Variation] = None
  var id: Option[String] = None
  var speed: Int = 1
  var deck: Option[List[Card]] = None
  val streets = new java.util.ArrayList[String]()

  def currentStreet = {
    if (streets.isEmpty) throw ReplayError("street not started yet")
    streets.get(streets.size - 1)
  }
  
  val actions = new java.util.HashMap[String, java.util.ArrayList[rpc.Request]]()
  var showdown: Boolean = false
  
  var processor: Function1[Token, Unit] = processMain
  
  def process(t: Token) = processor(t)

  def processMain(t: Token): Unit = t match {
    case tags.Table(_id, size) =>
      table = Some(new Table(size))
      id = Some(_id.unquote)
      processor = processTable
      
    case tags.Speed(duration) =>
      if (duration >= 0 && duration <= 10)
        speed = duration
      
    case tags.Street(name) =>
      streets.add(name)
      actions.put(name, new java.util.ArrayList[rpc.Request]())
      processor = processStreet
    
    case tags.Showdown() =>
      showdown = true
      
    case tags.Deck(cards) =>
      deck = Some(cards)
      
    case x =>
      Console printf("UNHANDLED: %s\n", x)
  }
  
  def bet(player: String, bet: Bet): Unit = {
    val t = table.getOrElse(throw ReplayError("betting before TABLE"))
    
    t.seat(player).map { case (seat, pos) =>
      actions.get(currentStreet).add(rpc.AddBet(seat.player.get, bet))
    }
  }

  def processStreet(tok: Token) = tok match {
    case tags.Ante(player) =>
      bet(player.unquote, Bet.ante(stake.get.smallBlind))
    
    case _: BettingSemantic =>
      val t = table.getOrElse(throw ReplayError("betting before TABLE"))
      val s = stake.getOrElse(throw ReplayError("STAKE is required"))
      tok match {
        case _: tags.Antes =>
          t.seatsAsList.map { seat =>
            if (!seat.isEmpty) {
              val ante = Bet.ante(s.smallBlind)
              actions.get(currentStreet).add(rpc.AddBet(seat.player.get, ante))
            }
          }
          
        case tags.Sb(player) =>
          bet(player.unquote, Bet.sb(s.smallBlind))
          
        case tags.Bb(player) =>
          val s = stake.getOrElse(throw ReplayError("STAKE is required"))
          bet(player.unquote, Bet.bb(s.bigBlind))
          
        case tags.Raise(player, amount) =>
          bet(player.unquote, Bet.raise(amount))
          
        case tags.AllIn(player) =>
          bet(player.unquote, Bet.allin)
          
        case tags.Call(player, amount) =>
          bet(player.unquote, Bet.call(amount))
        
        case tags.Check(player) =>
          bet(player.unquote, Bet.check)
          
        case tags.Fold(player) =>
          bet(player.unquote, Bet.fold)
      }
      
    case tags.Deal(player, cards, cardsNum) =>
      
      val action = if (player != null)
        rpc.DealCards(wire.DealType.HOLE, player.unquote, cards, cardsNum)
      else
        rpc.DealCards(wire.DealType.BOARD, null, cards, null)
        
      actions.get(currentStreet).add(action)

    case s @ tags.Street(name) =>
      processor = processMain
      process(s)

    case x =>
      processor = processMain
      process(x)
  }

  def processTable(t: Token) = t match {
    case tags.Seat(pos, uuid, stack) =>
      val t = table.getOrElse(throw ReplayError("SEAT is declared before TABLE"))
      val player = Player(uuid.unquote)
      //val pos: Int = t.button
      t.addPlayer(pos, player, Some(stack))
      //node ! rpc.JoinPlayer(player, t.button, Some(stack))
      t.button.move()
      
    case tags.Stake(sb, bb, ante) =>
      stake = Some(Stake(bb,
          Some(sb),
          Ante = ante match {
            case Some(n) => Left(n)
            case None => Right(false)
          }))
          
    case tags.Game(game, limit) =>
      val t = table.getOrElse(throw ReplayError("GAME is declared before TABLE"))
      val g = game.getOrElse(throw ReplayError("Unknown game"))
      variation = Some(Game(g, limit, Some(t.size)))
      
    case tags.Button(pos) =>
      val t = table.getOrElse(throw ReplayError("BUTTON is declared before TABLE"))
      t.button.current = pos
      
    case x: Any =>
      processor = processMain
      process(x)
  }

}
