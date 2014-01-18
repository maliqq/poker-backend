package de.pokerno.gameplay
import akka.actor.{Actor, ActorLogging, ActorRef, Props, FSM}
import de.pokerno.model._
import de.pokerno.protocol.{msg => message}

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

  val events = new EventBus
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
      stay
  }

  when(Instance.Waiting) {
    case Event(join: message.JoinTable, _) ⇒
      stay // TODO
  }

  when(Instance.Running) {
    case Event(Instance.Pause, _) ⇒
      goto(Instance.Paused)

    case Event(Instance.Stop, _) ⇒
      goto(Instance.Closed)

    case Event(Deal.Start, _) ⇒
      val gameplay = new Gameplay(events, variation, stake, table)
      val running = actorOf(Props(classOf[GameplayActor], gameplay), name = "gameplay-process")
      stay using Instance.Run(running)

    case Event(Deal.Done, _) ⇒
      val after = (5 seconds)
      log.info("deal done; starting next deal in %s".format(after))
      self ! Deal.Next(after)
      stay

    case Event(Deal.Next(after), _) ⇒
      system.scheduler.scheduleOnce(after, self, Deal.Start)
      stay
  }

  when(Instance.Closed) {
    case _ ⇒ stay
  }

  whenUnhandled {
    case Event(join: message.JoinTable, _) ⇒
      table.addPlayer(join.player, join.pos, Some(join.amount))

      events.publish(join)
      events.subscribe(sender, join.player.id)

      stay

    case Event(msg: message.AddBet, Instance.Run(running)) ⇒
      running ! msg
      stay
    
    case Event(Instance.Subscribe(ref, name), _) =>
      events.subscribe(ref, name)
      stay

    case Event(msg: message.Chat, _) ⇒
      events.publish(msg)
      stay
  }

  onTransition {
    case Instance.Created -> Instance.Running ⇒
      self ! Deal.Start // start deal immediately if possible

    case Instance.Waiting -> Instance.Running ⇒
      val after = (15 seconds)
      log.info("starting next deal in %s".format(after))
      self ! Deal.Next(after) // start deal after timeout
  }

  initialize

  def isReady: Boolean = table.seats.asInstanceOf[List[Seat]].filter(_ isReady).size == 2
}
