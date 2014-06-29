package de.pokerno.gameplay.tournament

trait Metrics {
  
  import com.codahale.metrics._
  
  val registry = new MetricRegistry
  
  val entrants  = registry.counter("entrants")
  val tables    = registry.counter("tables")
  val players   = registry.counter("players")
  val rebuys    = registry.counter("rebuys")
  val addons    = registry.counter("addons")
  val chips     = registry.counter("chips")

}
