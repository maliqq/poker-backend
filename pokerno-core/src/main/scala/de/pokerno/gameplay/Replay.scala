package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{msg, rpc}
import protocol.Conversions._
import protocol.wire.Conversions._
import protocol.msg.Conversions._
import de.pokerno.model._
import akka.actor.{Actor, Props, ActorRef, ActorLogging}

object Replay {
  case class Subscribe(out: ActorRef)
  case class Street(actions: List[rpc.Request], speed: Int)
}

class Replay(val gameplay: Context) extends Actor
      with ActorLogging
      with Dealing.ReplayContext
      with Streets.ReplayContext {
  
  lazy val stageContext = StageContext(gameplay, self)
  def e = gameplay.events
  def t = gameplay.table
  
  override def preStart {
    log.info("starting replay with gameplay {}", gameplay)
  }

  override def receive = {
    case Replay.Subscribe(out) =>
      log.info("subscribe")

      e.broker.subscribe(out, "replay-out")
      e.start(t, gameplay.variation, gameplay.stake)

    case join @ rpc.JoinPlayer(pos, player, amount) =>
      log.info("got: {}", join)

      t.addPlayer(pos, player, Some(amount))
      e.joinTable((player, pos), amount)
//
//    case addBet @ rpc.AddBet(player, bet) =>
//      log.info("got: {}", addBet)
//
//      if (bet.isForced) {
//
//        val (seat, pos) = t.seat(player).get
//        gameplay.round.forceBet((seat, pos), bet.betType.asInstanceOf[Bet.ForcedBet])
//
//      } else gameplay.round.addBet(bet)
//      
//      e.addBet(t.box(player).get, bet)

    case s @ rpc.ShowCards(cards, player, muck) =>
      
      log.debug("got: {}", s)
      
      e.showCards(t.box(player).get, cards, muck)

    case d @ rpc.DealCards(_type, player, cards, cardsNum) =>
      
      log.debug("got: {}", d)
      
      dealCards(_type, player, cards, cardsNum)
//
//    case Streets.Next =>
//      log.debug("streets next")
//
//      gameplay.prepareSeats(stageContext)
//      streets(stageContext)
//      
//    case Streets.Done =>
//      log.debug("showdown")
//
//      gameplay.showdown
//      context.stop(self)

    case x =>
      log.warning("unandled: {}", x)
  }

  override def postStop {
    log.info("actor stopped!")
  }
  
}
