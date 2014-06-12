package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonValue, JsonIgnore, JsonCreator}

abstract class MixType(val name: String) {
  @JsonValue override def toString = name
  @JsonIgnore lazy val options = Mixes(this)
}

object MixType {
  
  case object Horse extends MixType("horse")
  case object Eight extends MixType("eight")
  
  implicit def string2Mixed(v: String): MixType = v match {
    case "eight" | "8-game" | "eight-game" ⇒
      Eight
    case "horse" ⇒
      Horse
    case _ ⇒ null // throw?
  }
  
}
