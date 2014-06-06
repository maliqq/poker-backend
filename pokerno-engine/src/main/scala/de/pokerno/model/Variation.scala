package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonTypeInfo, JsonSubTypes}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes(Array(
    new JsonSubTypes.Type(name = "game", value = classOf[Game]),
    new JsonSubTypes.Type(name = "mix", value = classOf[Mix])
))
trait Variation {
  @JsonIgnore def isMixed: Boolean = this.isInstanceOf[Mix]
  def tableSize: Int
}
