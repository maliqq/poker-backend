package de.pokerno.model.autoplay

trait SitOut {
  
  private var _willSitOut = false
  
  def willSitOut = _willSitOut
  
  def sitOut() {
    if (!willSitOut) _willSitOut = true
  }
  
  def toggleSitOut() {
    _willSitOut = !willSitOut
  }
  
}
