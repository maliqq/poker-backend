package de.pokerno.model.tournament

case class Level(
  smallBlind: Int,
  bigBlind: Int,
  ante: Option[Int] = None) {
}
