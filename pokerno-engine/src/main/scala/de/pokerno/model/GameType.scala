package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonCreator, JsonIgnore, JsonValue}

trait GameType {
  @JsonValue def name: String
  override def toString = name
  lazy val options = Games(this)
}

object GameType {
  case object Texas extends GameType {
    def name = "texas"
  }
  case object Omaha extends GameType {
    def name = "omaha"
  }
  case object Omaha8 extends GameType {
    def name = "omaha8"
  }

  case object Stud extends GameType {
    def name = "stud"
  }
  case object Stud8 extends GameType {
    def name = "stud8"
  }
  case object Razz extends GameType {
    def name = "razz"
  }
  case object London extends GameType {
    def name = "london"
  }

  case object FiveCard extends GameType {
    def name = "five-card"
  }
  case object Single27 extends GameType {
    def name = "single27"
  }
  case object Triple27 extends GameType {
    def name = "triple27"
  }
  case object Badugi extends GameType {
    def name = "badugi"
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
    case "five-card" | "fivecard" ⇒
      FiveCard
    case "single27" | "single-27" ⇒
      Single27
    case "triple27" | "triple-27" ⇒
      Triple27
    case "badugi" ⇒
      Badugi
    case _ ⇒
      throw new Exception("can't build game type: %s" format(v))
  }

}
