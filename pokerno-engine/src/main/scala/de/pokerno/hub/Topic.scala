package de.pokerno.hub

trait Topic[T] extends Exchange[T] {

  def name: String

}
