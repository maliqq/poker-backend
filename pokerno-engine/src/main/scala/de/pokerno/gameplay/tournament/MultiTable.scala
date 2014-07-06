package de.pokerno.gameplay.tournament

trait MultiTable {
  
  def tableSize: Int
  def entrantsCount: Int
  
  def isMultiTable = entrantsCount > tableSize

}
