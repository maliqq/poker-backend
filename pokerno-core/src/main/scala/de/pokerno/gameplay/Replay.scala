package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{msg, rpc}
import de.pokerno.model._
import akka.actor.{Actor, Props, ActorRef, ActorLogging}

object Replay {
  case class Subscribe(out: ActorRef)
}

class Replay(val variation: Variation, val stake: Stake) extends Actor with ActorLogging {
  
  import protocol.Conversions._
  import protocol.wire.Conversions._
  import protocol.msg.Conversions._
  
  import context._
  
  val table = new Table(variation.tableSize)
  val gameplay = new Gameplay(events, variation, stake, table)
  
  val events = new GameplayEvents
  val dealer = new Dealer

  val betting = context.actorOf(Props(classOf[BettingActor], gameplay.round), name = "betting-process")

  override def preStart {
    log.info("starting replay with variation={} stake={}", variation, stake)
  }

  def receive = {
    case Replay.Subscribe(out) =>
      log.info("subscribe")
      events.broker.subscribe(out, "replay-out")
      events.start(table, variation, stake)

    case join @ rpc.JoinPlayer(pos, player, amount) =>
      log.info("got: {}", join)
      table.addPlayer(pos, player, Some(amount))
      events.joinTable((player, pos), amount)

    case addBet @ rpc.AddBet(player, bet) =>
      log.info("got: {}", addBet)
      if (bet.isForced) {

        val (seat, pos) = table.seat(player).get

        gameplay.round.forceBet((seat, pos), bet.betType.asInstanceOf[Bet.ForcedBet])

      } else betting ! addBet
      events.addBet(table.box(player).get, bet)

    case s @ rpc.ShowCards(cards, player, muck) =>
      log.debug("got: {}", s)
      events.showCards(table.box(player).get, cards, muck)

    case d @ rpc.DealCards(_type, player, cards, cardsNum) =>
      log.debug("got: {}", d)
      (_type: DealCards.Value) match {
        case DealCards.Hole =>
          log.debug(" | deal {} -> {}", cards, player)
          dealer.dealPocket(cards, player)
          events.dealCards(_type, cards, table.box(player))

        case DealCards.Door =>
          log.debug(" | deal door {} -> {}", cards, player)
          dealer.dealPocket(cards, player)
          events.dealCards(_type, cards, table.box(player))

        case DealCards.Board =>
          log.debug(" | deal board {}", cards)
          dealer.dealBoard(cards)
          events.dealCards(_type, cards)

        case x =>
          Console printf("\n\nWHAT THE FUCK?????%s\n\n", x)
      }

    case Street.Start =>
      log.debug("street start")
      gameplay.prepareSeats
      self ! Street.Next

    case Street.Next =>
      log.debug("next street")
      betting ! Betting.Next

    case Street.Exit =>
      log.debug("showdown")
      gameplay.showdown
      stop(self)

    case x => log.warning("unandled: {}", x)
  }

  override def postStop {
    log.info("actor stopped!")
  }
  
}
