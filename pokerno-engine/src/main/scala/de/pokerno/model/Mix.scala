package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty}
import beans._

object Mix {
  final val MaxTableSize = 8
}

case class Mix(
    @JsonProperty `type`: Game.Mixed,
    @JsonIgnore var _tableSize: Int = Mix.MaxTableSize
    ) extends Variation {
  @JsonIgnore val options = Mixes(`type`)
  if (_tableSize > Mix.MaxTableSize)
    _tableSize = Mix.MaxTableSize
  @JsonProperty val tableSize = _tableSize
  @JsonIgnore val games = options.map { option ⇒
    new Game(option._1, Some(option._2), Some(tableSize))
  }
  override def toString = "%s %s-max" format (`type`.toString, tableSize)
}
