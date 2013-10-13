package pokerno.backend.engine

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }

class GameplayActor(val gameplay: Gameplay) extends Actor with ActorLogging {
  import context._

  private val streets = Streets.ByGameGroup(gameplay.game.options.group)
  private val streetsIterator = streets iterator

  var currentStreet: ActorRef = system deadLetters

  val stages: List[Stage] = List(
      new Stage {
        def name = "prepare-seats"
        def run(context: Stage.Context) {
          context.gameplay.prepareSeats
        }
      },
      new Stage {
        def name = "rotate-game"
        def run(context: Stage.Context) {
          context.gameplay.rotateGame
        }
      },
      new Stage {
        def name = "post-antes"
        def run(context: Stage.Context) {
          context.gameplay.postAntes
        }
      },
      new Stage {
        def name = "post-blinds"
        def run(context: Stage.Context) {
          context.gameplay.postBlinds
        }
      }
  )
  
  override def preStart = {
    for (stage <- stages) {
      stage proceed(Stage.Context(gameplay = gameplay, street = self))
    }
    self ! Street.Next
  }

  def receive = {
    case Street.Next ⇒
      log.info("next street")

      if (sender != self)
        stop(currentStreet)

      if (streetsIterator hasNext) {
        val Street(name, stages) = streetsIterator.next
        currentStreet = actorOf(Props(classOf[StreetActor], gameplay, name, stages), name = "street-%s" format (name))
        currentStreet ! Stage.Next
      } else
        self ! Street.Exit

    case Street.Exit ⇒
      log.info("showdown")
      stop(self)
  }

  override def postStop {
    log.info("stop gameplay")
    parent ! Deal.Done
  }
}
