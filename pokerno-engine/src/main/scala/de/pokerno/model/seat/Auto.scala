package de.pokerno.model.seat

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonGetter, JsonInclude}
import de.pokerno.model.{Seat, Player, AutoPlay}

class Auto(_pos: Int, _player: Player) extends Acting(_pos, _player) {
  
  private var _autoplay = new AutoPlay.Play()
  def autoplay = _autoplay
  
  private var _sitOut = new AutoPlay.SitOut()
  def sitOut = _sitOut
  @JsonGetter def willSitOut: Option[Boolean] = if (_sitOut.willSitOut) Some(true) else None
  
}
