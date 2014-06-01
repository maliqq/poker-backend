package de.pokerno.protocol.game_events

sealed case class DeclarePlayStart(
    play: PlayState
) extends GameEvent {}
