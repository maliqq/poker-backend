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
    deck: Option[Cards])
  extends Actor with ActorLogging {

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
    log.info("starting replay {}", id)
    ctx broadcast Events.playStart()
    //gameplay.rotateGame(stageContext)
  }

  override def receive = {
    case Replay.Observe(out) ⇒
      events.broker.subscribe(out, "replay-out")
      events.broadcast(Events.start(table, variation, stake))

    // case join @ cmd.JoinPlayer(pos, player, amount) ⇒
    //   table.takeSeat(pos, player, Some(amount))
    //   ctx broadcast Events.playerJoin(pos, player, amount)
  
    case show @ cmd.ShowCards(cards, player, muck) ⇒
      table.playerPos(player) map { pos =>
        events.broadcast(Events.showCards(pos, player, cards, muck))
      }
    
//    case Betting.Stop ⇒ // идем до шоудауна
//      log.info("streets done")
//      Showdown(ctx)()
//      context.stop(self)

    case a @ Replay.Street(street, actions, _speed) ⇒

      log.debug("actions: {}", actions)

      speed = (_speed seconds)
      if (!bettingStarted) {
        PrepareSeats(ctx)()
      }

      if (streets.head == street) {
        // нужный стрит
        streets = streets.drop(1)
        log.info("[street] {}", street)
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
    log.info("replay {} stopped", id)
  }
}