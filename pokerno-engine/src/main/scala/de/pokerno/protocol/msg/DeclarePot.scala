package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude}
import de.pokerno.model.{Pot, SidePot}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclarePot(
    @JsonProperty pot: Pot,

    @JsonProperty rake: Option[SidePot] = None
  ) extends GameEvent {}
