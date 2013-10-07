package pokerno.backend.model

class Round(size: Int) {
  protected var _current: Int = 0

  def current = _current
  def current_=(at: Int) {
    _current = at
    _current %= size
  }

  def reset = {
    _current = 0
  }

  def move = {
    _current += 1
    _current %= size
  }
}
