package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonCreator}
import beans._

object Mix {
  final val MaxTableSize = 8
  
  def apply(`type`: MixType): Mix = Mix(`type`, MaxTableSize)
}

case class Mix(
    @JsonProperty `type`: MixType,
    @JsonProperty tableSize: Int
    ) extends Variation {
  
  def options = `type`.options
  
  @JsonIgnore val games = options.map { option ⇒
                            new Game(option._1, option._2, tableSize)
                          }

  override def toString = "%s %s-max" format (`type`.toString, tableSize)
  
  @JsonCreator
  def this(
      @JsonProperty("type") `type`: String,
      @JsonProperty("tableSize") tableSize: Int
    ) = this(`type`: MixType, tableSize)
}
