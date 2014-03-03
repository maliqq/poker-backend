package de.pokerno.gameplay

import akka.actor.{Actor, Props}
import concurrent.duration._
import de.pokerno.model.Table

/**
 * Simple deal cycle
 */

trait DealCycle {
a: Actor =>
  
  import context._
  
  final val minimumReadyPlayersToStart = 2
  final val firstDealAfter = (10 seconds)
  final val nextDealAfter = (5 seconds)
  
  def table: Table
  
  protected def canStart: Boolean = {
    table.seatsAsList.count(_ isReady) == minimumReadyPlayersToStart
  }
  
}
