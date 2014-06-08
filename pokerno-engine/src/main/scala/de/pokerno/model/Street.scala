package de.pokerno.model

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

class StreetRef extends TypeReference[Street.type]

object Street extends Enumeration {
  private def value(name: String) = new Val(nextId, name)

  // holdem poker
  val Preflop     = value("preflop")
  val Flop        = value("flop")
  val Turn        = value("turn")
  val River       = value("river")

  // seven card stud
  val Second      = value("second")
  val Third       = value("third")
  val Fourth      = value("fourth")
  val Fifth       = value("fifth")
  val Sixth       = value("sixth")
  val Seventh     = value("seventh")

  // draw poker
  val Predraw     = value("predraw")
  val Draw        = value("draw")
  val FirstDraw   = value("first-draw")
  val SecondDraw  = value("second-draw")
  val ThirdDraw   = value("third-draw")

  implicit def string2streetValueOption(s: String): Option[Value] = values.find(_.toString == s)

  final val byGameGroup = Map[Game.Group, Seq[Value]](
    Game.Holdem ->
      Seq(Preflop, Flop, Turn, River),
    Game.SevenCard ->
      Seq(Second, Third, Fourth, Fifth, Sixth, Seventh),
    Game.SingleDraw ->
      Seq(Predraw, Draw),
    Game.TripleDraw ->
      Seq(Predraw, FirstDraw, SecondDraw, ThirdDraw)
  )
}
