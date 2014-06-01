package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.Street

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

class StreetRef extends TypeReference[Street.type]

sealed case class DeclareStreet(
    @JsonScalaEnumeration(classOf[StreetRef]) @BeanProperty name: Street.Value
) extends GameEvent {}
