package de.pokerno.model.autoplay

trait Leave {
  private var _leaving = false
  def isLeaving = _leaving
  
  def leaving() = {
    _leaving = true
  }
}
