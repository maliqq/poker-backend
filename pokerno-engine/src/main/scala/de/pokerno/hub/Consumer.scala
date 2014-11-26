package de.pokerno.hub

trait Consumer[T] {
  def consume(msg: T)
}
