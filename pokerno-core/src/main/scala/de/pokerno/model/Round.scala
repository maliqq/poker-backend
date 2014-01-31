package de.pokerno.model

class Round(size: Int) {
  protected var _current: Int = 0

  def current = _current
  def current_=(at: Int) {
    _current = if (at < 0) size + at else at
    _current %= size
  }

  def reset() {
    _current = 0
  }

  def move() {
    _current += 1
    _current %= size
  }
}

object Round {
  implicit def current2Int(round: Round): Int = round.current
  implicit def current2Integer(round: Round): Integer = round.current
}
