package de.pokerno.model.table.seat

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonGetter, JsonInclude}

import de.pokerno.model.autoplay.{Play => AutoPlay}

class Auto(_pos: Int, _player: Player) extends Acting(_pos, _player) {
  
  private var _autoplay = new AutoPlay()
  def autoplay = _autoplay
  
}
