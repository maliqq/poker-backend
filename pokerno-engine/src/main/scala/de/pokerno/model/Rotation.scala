package de.pokerno.model

object Rotation {
  final val DefaultRotateEvery = 8
}

class Rotation[A](
    items: Seq[A],
    rotateEvery: Int = Rotation.DefaultRotateEvery,
    start: Int = 0,
    counter: Int = 0) extends Ring[A](items) {
  
  private var _counter  = counter
  
  override def hasNext = {
    _counter += 1
    if (_counter > rotateEvery) {
      _counter = 0
    }
    _counter == 0
  }
  
}
