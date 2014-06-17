package de.pokerno.model.seat

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonGetter, JsonInclude}
import de.pokerno.model.{Seat, Player}
import de.pokerno.model.autoplay.{Play => AutoPlay, SitOut, Leave, Stack}

class Auto(_pos: Int, _player: Player) extends Acting(_pos, _player) with SitOut with Leave {
  
  private var _autoplay = new AutoPlay()
  def autoplay = _autoplay
  
}
