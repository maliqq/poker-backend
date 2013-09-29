package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

object Gameplay {
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
  
  class Context(
    val dealer: Dealer,
    val broadcast: Broadcast,
    val variation: Variation,
    val stake: Stake,
    val table: Table
  ) extends Rotation {
    
    var game: Game = variation match {
      case g: Game => g
      case m: Mix => m.games.head
    }
    
    def rotateGame {
      rotateNext { g =>
        game = g
        broadcast.all(Message.ChangeGame(game = game))
      }
    }
    
    def moveButton {
      table.moveButton
      broadcast.all(Message.MoveButton(pos = table.button))
    }
    
    def setButton(pos: Int) {
      table.button = pos
      broadcast.all(Message.MoveButton(pos = table.button))
    }
    
    def prepareSeats {
      table.seats foreach { (seat) =>
        seat.state match {
          case Seat.Ready | Seat.Play | Seat.Fold =>
            seat.play
          case _ =>
        }
      }
    }
  }
}

class Gameplay {
  
}
