package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{msg, rpc}
import de.pokerno.model._
import akka.actor.{Actor, Props, ActorRef, ActorLogging}

object Replay {
  case class Subscribe(out: ActorRef)
}

class Replay(_gameplay: GameplayContext) extends DealActor(_gameplay) {
  
  import protocol.Conversions._
  import protocol.wire.Conversions._
  import protocol.msg.Conversions._
  
  override def preStart {
    log.info("starting replay with gameplay {}", gameplay)
  }
  
  def e = gameplay.events
  def t = gameplay.table

  override def receive = {
    case Replay.Subscribe(out) =>
      log.info("subscribe")

      e.broker.subscribe(out, "replay-out")
      e.start(t, gameplay.variation, gameplay.stake)

    case join @ rpc.JoinPlayer(pos, player, amount) =>
      log.info("got: {}", join)

      t.addPlayer(pos, player, Some(amount))
      e.joinTable((player, pos), amount)

    case addBet @ rpc.AddBet(player, bet) =>
      log.info("got: {}", addBet)

      if (bet.isForced) {

        val (seat, pos) = t.seat(player).get
        gameplay.round.forceBet((seat, pos), bet.betType.asInstanceOf[Bet.ForcedBet])

      } else gameplay.round.addBet(bet)
      e.addBet(t.box(player).get, bet)

    case s @ rpc.ShowCards(cards, player, muck) =>
      log.debug("got: {}", s)

      e.showCards(t.box(player).get, cards, muck)

    case d @ rpc.DealCards(_type, player, cards, cardsNum) =>
      log.debug("got: {}", d)

      (_type: DealCards.Value) match {
        case DealCards.Hole =>
          log.debug(" | deal {} -> {}", cards, player)
          gameplay.dealer.dealPocket(cards, player)
          e.dealCards(_type, cards, t.box(player))

        case DealCards.Door =>
          log.debug(" | deal door {} -> {}", cards, player)
          gameplay.dealer.dealPocket(cards, player)
          e.dealCards(_type, cards, t.box(player))

        case DealCards.Board =>
          log.debug(" | deal board {}", cards)
          gameplay.dealer.dealBoard(cards)
          e.dealCards(_type, cards)
      }

    case Streets.Next =>
      log.debug("streets next")

      gameplay.prepareSeats(stageContext)
      streets(stageContext)
      
    case Streets.Done =>
      log.debug("showdown")

      gameplay.showdown
      context.stop(self)

    case x =>
      log.warning("unandled: {}", x)
  }

  override def postStop {
    log.info("actor stopped!")
  }
  
}
