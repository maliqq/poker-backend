package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

trait GameRotation {
  g: Gameplay ⇒
  def variation: Variation

  final val rotateEvery = 8

  private var _rotationIndex = 0
  private var _rotationCounter = 0

  protected def rotateNext(f: Game ⇒ Unit) {
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

  def rotateGame = if (variation isMixed)
    rotateNext { g ⇒
      game = g
      broadcast all (Message.ChangeGame(game = game))
    }
}
