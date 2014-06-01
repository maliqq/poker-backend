package de.pokerno.model

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

object DealType extends Enumeration {
  val Board, Door, Hole, Discard = Value
}
