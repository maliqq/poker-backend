package de.pokerno.gameplay.tournament

trait SingleTableTournament {
  /*
  Одностоловый турнир
  */
  
  def tableSize: Int
  def maxEntrantsCount: Int
  
  def isSingleTable = tableSize == maxEntrantsCount
}
