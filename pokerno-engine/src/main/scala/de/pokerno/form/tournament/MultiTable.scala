package de.pokerno.form.tournament

trait MultiTable {
  
  def tableSize: Int
  def entrantsCount: Int
  
  def isMultiTable = entrantsCount > tableSize

}
