package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.JsonCreator

object Chat {
  @JsonCreator def apply(message: String) = new Chat(message)
  def unapply(chat: Chat): Option[Tuple1[String]] = Some(Tuple1(chat.message))
}

sealed class Chat(
  val message: String
) extends PlayerEvent {}
