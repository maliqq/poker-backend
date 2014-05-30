package de.pokerno.model

class Ring[A](items: Seq[A]) extends Iterator[A] {
  protected var _current = 0
  protected var _size = items.size 
  
  def current = _current
  def current_=(i: Int) = {
    _current = if (i < 0) _size + i else i
    _current %= _size
    i
  }

  def reset() {
    _current = 0
  }
  
  def next = {
    val value = items(_current) 
    _current += 1
    _current %= _size
    value
  }
  
  def hasNext = !items.isEmpty
}

object Ring {
  implicit def current2Int[T](ring: Ring[T]): Int = ring.current
}
