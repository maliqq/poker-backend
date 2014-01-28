package de.pokerno.gameplay
import akka.actor.{Actor, ActorLogging, ActorRef, Props, FSM}
import de.pokerno.model._
import de.pokerno.protocol.Conversions.{wire2player, wire2bet, wire2decimal}
import de.pokerno.protocol.{msg, rpc}

import concurrent.duration._

object Instance {

  sealed trait State

  case object Created extends State
  case object Waiting extends State
  case object Running extends State
  case object Paused extends State
  case object Closed extends State

  sealed trait Data
  case object Empty extends Data
  case class Run(running: ActorRef) extends Data

  case object Stop
  case object Pause
  case object Resume
  case object Start
  
  case class Subscribe(ref: ActorRef, name: String)
  
}

class Instance(val variation: Variation, val stake: Stake) extends Actor with ActorLogging with FSM[Instance.State, Instance.Data] {
  import context._
  import context.dispatcher

  val events = new GameplayEvents
  val table = new Table(variation.tableSize)

  startWith(Instance.Created, Instance.Empty)

  when(Instance.Created) {
    case Event(Instance.Start, _) ⇒
      goto(Instance.Running)
  }

  when(Instance.Paused) {
    case Event(Instance.Resume, _) ⇒
      goto(Instance.Running)

    case Event(Deal.Next, _) ⇒
      log.info("game is paused")
      stay()
  }

  when(Instance.Waiting) {
    case Event(join: rpc.JoinPlayer, _) ⇒
      stay() // TODO
  }

  when(Instance.Running) {
    case Event(Instance.Pause, _) ⇒
      goto(Instance.Paused)

    case Event(Instance.Stop, _) ⇒
      goto(Instance.Closed)

    case Event(Deal.Start, _) ⇒
      val gameplay = new GameplayContext(events, variation, stake, table)
      val running = actorOf(Props(classOf[DealActor], gameplay), name = "gameplay-process")
      stay using Instance.Run(running)

    case Event(Deal.Done, _) ⇒
      val after = (5 seconds)
      log.info("deal done; starting next deal in %s".format(after))
      self ! Deal.Next(after)
      stay()

    case Event(Deal.Next(after), _) ⇒
      system.scheduler.scheduleOnce(after, self, Deal.Start)
      stay()
  }

  when(Instance.Closed) {
    case _ ⇒ stay()
  }

  whenUnhandled {
    case Event(join: rpc.JoinPlayer, _) ⇒
      table.addPlayer(join.pos, join.player, Some(join.amount))

      events.joinTable((join.player, join.pos), join.amount)
      events.broker.subscribe(sender, join.player.id)

      stay()

    case Event(addBet: rpc.AddBet, Instance.Run(running)) ⇒
      running ! Betting.Add(addBet.bet)
      stay()
    
    case Event(Instance.Subscribe(ref, name), _) =>
      events.broker.subscribe(ref, name)
      stay()

    case Event(chat: rpc.Chat, _) ⇒ // FIXME
      //events.publish(msg)
      stay()
  }

  onTransition {
    case Instance.Created -> Instance.Running ⇒
      self ! Deal.Start // start deal immediately if possible

    case Instance.Waiting -> Instance.Running ⇒
      val after = (15 seconds)
      log.info("starting next deal in %s".format(after))
      self ! Deal.Next(after) // start deal after timeout
  }

  initialize()

  def isReady: Boolean = (table.seats: List[Seat]).filter(_ isReady).size == 2
}
