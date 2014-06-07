package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonCreator}
import beans._

object Mix {
  final val MaxTableSize = 8
  
  def apply(_type: Game.Mixed, _tableSize: Int = Mix.MaxTableSize) = new Mix(_type, _tableSize)
}

class Mix(
    @JsonProperty val `type`: Game.Mixed,
    _tableSize: Int = Mix.MaxTableSize
    ) extends Variation {
  @JsonIgnore     val options = Mixes(`type`)
  @JsonProperty   val tableSize = if (_tableSize > Mix.MaxTableSize) Mix.MaxTableSize else _tableSize
  @JsonIgnore     val games = options.map { option ⇒
                                new Game(option._1, Some(option._2), Some(tableSize))
                              }
  override def toString = "%s %s-max" format (`type`.toString, tableSize)
  
  @JsonCreator
  def this(
      @JsonProperty("type") _type: String,
      @JsonProperty("size") _size: Int
    ) = this(_type: Game.Mixed, _size)
}
