package de.pokerno.model

case class Action(
    player: Player,
    bet: Bet,
    at: java.util.Date = new java.util.Date()
)
