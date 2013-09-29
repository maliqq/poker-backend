package pokerno.backend.engine

trait Round[T] {
  private var _items: List[T] = List.empty
  private var _current: Int = 0
  
  def start(items: List[T]) = {
    _items = items
    _current = 0
  }
  
  def stop = {
    _current = 0
  }
  
  def step(f: T => Unit) = {
    f(current)
    move
  }
  
  def foreach(items: List[T]) (f: T => Unit) = {
    start(items)
    _items foreach { case item => 
      f(item)
    }
    stop
  }
  
  def current: T = _items(_current)
  def move = {
    _current += 1
    _current %= _items.size
  }
}
