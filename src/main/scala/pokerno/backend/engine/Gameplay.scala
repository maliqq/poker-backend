package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._
import akka.actor.{Actor, ActorRef}
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

object Gameplay {
  case object Start
  case object NextStreet
  case object Showdown
  
  class Process(val gameplay: Gameplay) extends Actor {
    import context._

    private var _streetIndex = 0
    def streets = Streets.ByGameGroup(gameplay.game.options.group)
    
    override def preStart {
      gameplay.table.where(_.isReady).map(_._1.play)
      gameplay.rotateNext { game =>
        gameplay.game = game
        gameplay.broadcast.all(Message.ChangeGame(game = game))
      }
    }
    
    def receive = {
      case NextStreet =>
        _streetIndex += 1
        if (_streetIndex > streets.size)
          stop(self)
        else
          streets(_streetIndex).run(gameplay)
    }
    
    override def postStop {
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
