package de.pokerno.replay

import de.pokerno.poker.{ Deck, Card, Cards }
import de.pokerno.model._
import de.pokerno.protocol
import de.pokerno.protocol.cmd
import de.pokerno.gameplay.{Events, Context => Gameplay, stg}
import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

object Replay {
  case class Observe(out: ActorRef)
  
  case class Street(
      // which street to play
      street: de.pokerno.model.Street.Value,
      actions: Seq[protocol.Command],
      speed: Int
    )

  case object Showdown  
  case object Stop
}

class Replay(
    id: String,
    table: Table, variation: Variation, stake: Stake,
    deck: Option[Cards]
  )   extends Actor
      with ActorLogging {

  val dealer: Dealer = deck.map { cards =>
      new Dealer(new Deck(cards)) 
    } getOrElse new Dealer
  
  val gameplay = new Gameplay(table, variation, stake, new Events(id), dealer)

  val ctx = new Context(gameplay, self)
  import ctx._
  import ctx.gameplay._

  import concurrent.duration._
  import de.pokerno.gameplay.stages.{PrepareSeats, BringIn, Showdown}

  override def preStart {
    log.info("starting replay with gameplay %s", gameplay)
    //e.playStart()
    //gameplay.rotateGame(stageContext)
  }

  override def receive = {
    case Replay.Observe(out) ⇒
      events.broker.subscribe(out, "replay-out")
      events.broadcast(Events.start(table, variation, stake))

    case join @ cmd.JoinPlayer(pos, player, amount) ⇒
      log.debug("got: {}", join)
  
      table.takeSeat(pos, player, Some(amount))
      Events.playerJoin(pos, player, amount)
  
    case s @ cmd.ShowCards(cards, player, muck) ⇒
  
      log.debug("got: %s", s)
      table.playerPos(player) map { pos =>
        events.broadcast(Events.showCards(pos, player, cards, muck))
      }
    
//    case Betting.Stop ⇒ // идем до шоудауна
//      log.info("streets done")
//      Showdown(ctx)()
//      context.stop(self)

    case a @ Replay.Street(street, actions, _speed) ⇒

      speed = (_speed seconds)
      if (isFirstStreet) {
        PrepareSeats(ctx)()
      }

      if (streets.head == street) {
        // нужный стрит
        streets = streets.drop(1)
        events.broadcast(Events.streetStart(street))

        Streets.buildStages(street, actions)(ctx)
      }

    case Replay.Showdown ⇒
      log.info("[showdown] start")
      Showdown(ctx)()
      log.info("[showdown] stop")

    case Replay.Stop ⇒
      events broadcast Events.playStop()
      context stop self

    case x ⇒ log.warning("unandled: {}", x)
  }

  override def postStop {
  }
}
