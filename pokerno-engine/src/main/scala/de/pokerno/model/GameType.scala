package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

abstract class GameType(@JsonValue val name: String) {
  override def toString = name
}

object GameType {
  case object Texas extends GameType("texas")
  case object Omaha extends GameType("omaha")
  case object Omaha8 extends GameType("omaha8")

  case object Stud extends GameType("stud")
  case object Stud8 extends GameType("stud8")
  case object Razz extends GameType("razz")
  case object London extends GameType("london")

  case object FiveCard extends GameType("five-card")
  case object Single27 extends GameType("single27")
  case object Triple27 extends GameType("triple27")
  case object Badugi extends GameType("badugi")
  
  implicit def string2GameType(v: String): GameType = v match {
    case "texas" | "texas-holdem" | "holdem" ⇒
      Texas
    case "omaha" ⇒
      Omaha
    case "omaha8" | "omaha-8" ⇒
      Omaha8
    case "stud" ⇒
      Stud
    case "stud8" | "stud-8" ⇒
      Stud8
    case "razz" ⇒
      Razz
    case "london" ⇒
      London
    case "five-card" ⇒
      FiveCard
    case "single27" | "single-27" ⇒
      Single27
    case "triple27" | "triple-27" ⇒
      Triple27
    case "badugi" ⇒
      Badugi
    case _ ⇒
      null // throw?
  }

}
