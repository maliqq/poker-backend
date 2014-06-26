package de.pokerno.model

case class Action(
    player: Player,
    bet: Bet,
    at: java.util.Date = new java.util.Date(),
    isAllIn: Option[Boolean] = None,
    isForced: Option[Boolean] = None,
    isTimeout: Option[Boolean] = None
)
