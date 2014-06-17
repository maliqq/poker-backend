package de.pokerno.model.autoplay

trait Leave {
  private var _willLeave = false
  def willLeave = _willLeave
  
  def leave() = _willLeave = true
}
