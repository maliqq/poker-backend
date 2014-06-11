package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

abstract class MixType(@JsonValue val name: String) {
  override def toString = name
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
