package de.pokerno.model.seat

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonGetter, JsonInclude}
import de.pokerno.model.{Seat, Player}
import de.pokerno.model.autoplay.{Play => AutoPlay}

class Auto(_pos: Int, _player: Player) extends Acting(_pos, _player) {
  
  private var _autoplay = new AutoPlay()
  def autoplay = _autoplay
  
  private var _willSitOut = false
  
  def willSitOut = _willSitOut
  
  def sitOut() {
    if (!willSitOut) _willSitOut = true
  }
  
  def toggleSitOut() {
    _willSitOut = !willSitOut
  }
 
  
}
