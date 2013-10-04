package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
<<<<<<< HEAD
import akka.actor.{Actor, ActorRef}
=======
import akka.actor.{Actor, Props, ActorLogging, ActorRef}
>>>>>>> af770ee2c6fa519880c6fa6dd166f41337b04077
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

case class StageContext(val gameplay: Gameplay, betting: ActorRef, process: ActorRef)

object Gameplay {
  case object Start
  case object NextStreet
  case object Showdown
  case object Stop
  
  class Process(val gameplay: Gameplay) extends Actor with ActorLogging {
    import context._
    
    val streets = Streets.ByGameGroup(gameplay.game.options.group)
    
    private var _currentStreet = 0
    def currentStreet = streets(_currentStreet)
    
    def receive = {
      case Start =>
        gameplay.table.where(_.isReady).map(_._1.play)
        
        gameplay.rotateNext { g =>
          gameplay.game = g
          gameplay.broadcast.all(Message.ChangeGame(game = gameplay.game))
        }
        self ! NextStreet
      
      case NextStreet =>
        if (_currentStreet >= streets.size)
          self ! Showdown
        else {
          val street = currentStreet
          log.info("= street %s start\n", street.name)
          for (stage <- street.stages) {
            stage(gameplay, self)
          }
        }
        _currentStreet += 1
      
      case Showdown =>
        gameplay.showdown
        parent ! Deal.Done
    }
  }
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
}
