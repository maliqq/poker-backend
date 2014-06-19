package de.pokerno.gameplay.replay

import de.pokerno.poker.{ Deck, Card, Cards }
import de.pokerno.model._
import de.pokerno.finance.thrift.Balance.{FutureIface => Balance}
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
    table: Table, variation: Variation, stake: Stake, balance: Balance,
    deck: Option[Cards])
  extends Actor with ActorLogging {

  val dealer: Dealer = deck.map { cards =>
      new Dealer(new Deck(cards)) 
    } getOrElse new Dealer
  
  val gameplay = new Gameplay(id, table, variation, stake, balance, new Events(id), dealer)

  val ctx = new Context(gameplay, self)
  import ctx._
  import ctx.gameplay._

  import concurrent.duration._
  import de.pokerno.gameplay.stages.{PlayStart, BringIn, Showdown, PlayStop}

  override def preStart {
    log.info("starting replay {}", id)
    events broadcast Events.playStart(gameplay)
    //gameplay.rotateGame(stageContext)
  }

  override def receive = {
    case Replay.Observe(out) ⇒
      events.broker.subscribe(out, "replay-out")
      events broadcast Events.start(id, table, variation, stake)

    // case join @ cmd.JoinPlayer(pos, player, amount) ⇒
    //   table.takeSeat(pos, player, Some(amount))
    //   ctx broadcast Events.playerJoin(pos, player, amount)
  
    case show @ cmd.ShowCards(cards, player, muck) ⇒
      table.playerSeat(player) map { seat =>
        events broadcast Events.showCards(seat, cards, muck)
      }
    
//    case Betting.Stop ⇒ // идем до шоудауна
//      log.info("streets done")
//      Showdown(ctx)()
//      context.stop(self)

    case a @ Replay.Street(street, actions, _speed) ⇒

      log.debug("actions: {}", actions)

      speed = (_speed seconds)
      if (!bettingStarted) {
        PlayStart(ctx)()
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
      PlayStop(ctx)()
      context stop self

    case x ⇒ log.warning("unandled: {}", x)
  }

  override def postStop {
    log.info("replay {} stopped", id)
  }
}
