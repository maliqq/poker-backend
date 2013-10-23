package pokerno.backend.engine

import akka.actor.{ Actor, ActorSystem, ActorRef, Props, FSM }
import pokerno.backend.model.{ Variation, Stake, Table }
import pokerno.backend.protocol._

sealed trait State

case object Created extends State
case object Waiting extends State
case object Running extends State
case object Paused extends State
case object Closed extends State

sealed trait Data
case object Empty extends Data

object Instance {
  case object Stop
  case object Pause
  case object Resume
  case object Start
  case class JoinTable()
}

class Instance(val variation: Variation, val stake: Stake) extends Actor with FSM[State, Data] {
  import context._
  import context.dispatcher

  val events = new EventBus
  val table = new Table(variation.tableSize)
  var running: ActorRef = system deadLetters
  
  startWith(Created, Empty)

  when(Created) {
    case Event(Instance.Start, g: Gameplay) ⇒
      goto(Running) using g
  }

  when(Paused) {
    case Event(Instance.Resume, g: Gameplay) ⇒
      goto(Running) using g
  }

  when(Waiting) {
    case Event(join: Instance.JoinTable, g: Gameplay) ⇒
      stay using g
  }

  when(Running) {
    case Event(Instance.Pause, g: Gameplay) ⇒
      goto(Paused) using g

    case Event(Instance.Stop, g: Gameplay) ⇒
      goto(Closed) using g
  }

  onTransition {
    case Created -> Running ⇒
      stateData match {
        case g: Gameplay ⇒
        case _           ⇒
      }
  }
  
  def whenRunning: Receive = {
    case Deal.Start ⇒
      log.info("deal start")
      val gameplay = new Gameplay(events, variation, stake, table)
      running = actorOf(Props(classOf[GameplayActor], gameplay), name = "gameplay-process")

    case Message.SitOut   ⇒
    case Message.ComeBack ⇒
    case msg: Message.AddBet ⇒
      running ! msg

    case Message.ChatMessage ⇒
    case msg: Message.JoinTable ⇒
      gameplay.table.addPlayer(msg.player, msg.pos, Some(msg.amount))
      gameplay.broadcast (msg)
      gameplay.events.subscribe(sender, msg.player.id)
      
    case Message.LeaveTable ⇒
    case Message.KickPlayer ⇒

    case Deal.Done ⇒
      log.info("deal done - starting next deal in 5 seconds")
      system.scheduler scheduleOnce (5 seconds, self, Deal.Start)

    case Deal.Stop ⇒
      log.info("deal stop")
      stop(self)
  }

  initialize
}
