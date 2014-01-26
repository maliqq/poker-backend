package de.pokerno.replay

import de.pokerno.format.text.Lexer.{Token, Tags => tags}
import de.pokerno.model.{Player, Table, Stake, Variation, Game, Bet, Seat}
import de.pokerno.protocol._
import de.pokerno.protocol.Conversions._
import de.pokerno.protocol.wire.Conversions._
import de.pokerno.gameplay.Street
import wire.Conversions._

class Scenario {
  var table: Option[Table] = None
  var stake: Option[Stake] = None
  var variation: Option[Variation] = None
  var id: Option[String] = None
  var speed: Int = 1
  val streets = new java.util.ArrayList[String]()

  def currentStreet = {
    if (streets.isEmpty) throw ReplayError("street not started yet")
    streets.get(streets.size - 1)
  }
  
  val actions = new java.util.HashMap[String, java.util.ArrayList[rpc.Request]]()
  
  var processor: Function1[Token, Unit] = processMain
  
  def process(t: Token) = processor(t)

  def processMain(t: Token): Unit = t match {
    case tags.Table(_id, size) =>
      table = Some(new Table(size))
      id = Some(_id.unquote)
      processor = processTable
      
    case tags.Speed(duration) =>
      speed = duration
      
    case tags.Street(name) =>
      streets.add(name)
      actions.put(name, new java.util.ArrayList[rpc.Request]())
      processor = processStreet
      
    case x =>
      Console printf("UNHANDLED: %s\n", x)
  }
  
  def bet(player: String, bet: Bet): Unit = table.map { t =>
    t.seat(player).map { case (seat, pos) =>
      actions.get(currentStreet).add(rpc.AddBet(seat.player.get, bet))
    }
  }

  def processStreet(t: Token) = t match {
    case tags.Sb(player) =>
      bet(player.unquote, Bet.sb(stake.get.smallBlind))
      
    case tags.Bb(player) =>
      bet(player.unquote, Bet.bb(stake.get.bigBlind))
      
    case tags.Ante(player) =>
      bet(player.unquote, Bet.ante(stake.get.smallBlind))
      
    case tags.Raise(player, amount) =>
      bet(player.unquote, Bet.raise(amount))
      
    case tags.AllIn(player) =>
      bet(player.unquote, Bet.allin)
      
    case tags.Call(player, amount) =>
      bet(player.unquote, Bet.call(amount))
      
    case tags.Fold(player) =>
      bet(player.unquote, Bet.fold)
      
    case tags.Deal(player, cards, cardsNum) =>
      actions.get(currentStreet).add(rpc.DealCards(wire.DealType.HOLE, player.unquote, cards, cardsNum))

    case s @ tags.Street(name) =>
      processor = processMain
      process(s)

    case x =>
      processor = processMain
      process(x)
  }

  def processTable(t: Token) = t match {
    case tags.Seat(uuid, stack) =>
      table.map { t =>
        val player = Player(uuid.unquote)
        val pos = t.button
        t.addPlayer(pos, player, Some(stack))
        //node ! rpc.JoinPlayer(player, t.button, Some(stack))
        t.button.move
      }
      
    case tags.Stake(sb, bb, ante) =>
      stake = Some(Stake(sb,
          Some(bb),
          Ante = ante match {
            case Some(n) => Left(n)
            case None => Right(false)
          }))
          
    case tags.Game(game, limit) =>
      if (game.isDefined)
        variation = Some(Game(game.get, limit, Some(table.get.size)))
      else
        throw ReplayError("game unknown")

    case tags.Button(pos) =>
      table.map(_.button.current = pos)
      
    case x: Any =>
      processor = processMain
      process(x)
  }

}
