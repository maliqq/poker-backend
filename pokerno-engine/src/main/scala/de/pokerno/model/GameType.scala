package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

trait GameType

object GameType {
  case object Texas extends GameType {
    @JsonValue override def toString = "texas"
  }
  case object Omaha extends GameType {
    @JsonValue override def toString = "omaha"
  }
  case object Omaha8 extends GameType {
    @JsonValue override def toString = "omaha8"
  }

  case object Stud extends GameType {
    @JsonValue override def toString = "stud"
  }
  case object Stud8 extends GameType {
    @JsonValue override def toString = "stud8"
  }
  case object Razz extends GameType {
    @JsonValue override def toString = "razz"
  }
  case object London extends GameType {
    @JsonValue override def toString = "london"
  }

  case object FiveCard extends GameType {
    @JsonValue override def toString = "five-card"
  }
  case object Single27 extends GameType {
    @JsonValue override def toString = "single27"
  }
  case object Triple27 extends GameType {
    @JsonValue override def toString = "triple27"
  }
  case object Badugi extends GameType {
    @JsonValue override def toString = "badugi"
  }
  
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
