package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import de.pokerno.model.{Street, StreetRef}

sealed case class DeclareStreet(
    @JsonScalaEnumeration(classOf[StreetRef]) @JsonProperty name: Street.Value
) extends GameEvent {}
