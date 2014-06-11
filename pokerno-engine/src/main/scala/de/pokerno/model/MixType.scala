package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

trait MixType

object MixType {
  
  case object Horse extends MixType {
    @JsonValue override def toString = "horse"
  }
  case object Eight extends MixType {
    @JsonValue override def toString = "eight"
  }
  
  implicit def string2Mixed(v: String): MixType = v match {
    case "eight" | "8-game" | "eight-game" ⇒
      Eight
    case "horse" ⇒
      Horse
    case _ ⇒ null // throw?
  }
  
}
