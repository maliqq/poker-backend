package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.{Actor, Props, ActorLogging, ActorRef}
import scala.concurrent.Future

trait Rotation {
  def variation: Variation
  
  final val rotateEvery = 8
  
  private var _rotationIndex = 0
  private var _rotationCounter = 0
  
  protected def rotateNext(f: Game => Unit) {
    _rotationCounter += 1
    if (_rotationCounter > rotateEvery) {
      _rotationCounter = 0
      f(nextGame)
    }
  }
  
  private def nextGame = {
    val mix = variation.asInstanceOf[Mix]
    _rotationIndex += 1
    _rotationIndex %= mix.games.size
    mix.games(_rotationIndex)
  }
}

class GameplayActor(val gameplay: Gameplay) extends Actor with ActorLogging {
  import context._
  
  private val streets = Streets.ByGameGroup(gameplay.game.options.group)
  private val streetsIterator = streets.iterator
  
  var currentStreet: ActorRef = system.deadLetters
  
  override def preStart = {
    log.info("start gameplay")
    
    gameplay.table.where(_.isReady).map(_._1.play)
    gameplay.rotateGame
    
    self ! Street.Next
  }
  
  def receive = {
    case Street.Next =>
      log.info("next street")
      
      if (sender != self)
        stop(currentStreet)
      
      if (streetsIterator.hasNext) {
        val Street(name, stages) = streetsIterator.next
        currentStreet = actorOf(Props(classOf[StreetActor], gameplay, name, stages), name = "street-%s".format(name))
        currentStreet ! Stage.Next
      } else
        self ! Street.Exit
    
    case Street.Exit =>
      log.info("showdown")
      gameplay.showdown
      stop(self)
  }

  override def postStop {
    log.info("stop gameplay")
    parent ! Deal.Done
  }
}

object Gameplay {
  case object Start
  case object Stop
}

class Gameplay (
  val dealer: Dealer,
  val broadcast: Broadcast,
  val variation: Variation,
  val stake: Stake,
  val table: Table
) extends Rotation with Antes with Blinds with Showdown {
  
  var betting: Betting.Context = null
  
  var game: Game = variation match {
    case g: Game => g
    case m: Mix => m.games.head
  }
  
  def moveButton {
    table.moveButton
    broadcast.all(Message.MoveButton(pos = table.button))
  }
  
  def setButton(pos: Int) {
    table.button = pos
    broadcast.all(Message.MoveButton(pos = table.button))
  }
  
  def forceBet(betType: Bet.Value) {
    val bet = Bet.force(betType, stake)
    betting.force(bet)
    broadcast.all(Message.AddBet(Bet.Ante, pos = Some(betting.pos), bet = bet))
  }
  
  def rotateGame = if (variation.isMixed)
    rotateNext { g =>
      game = g
      broadcast.all(Message.ChangeGame(game = game))
    }
  
  def completeBetting {
    betting.clear
    
    table.where(_.inPlay) map(_._1.play)
  
    val total = betting.pot.total
    val message = Message.CollectPot(total = total)
    broadcast.all(message)
  }

}
