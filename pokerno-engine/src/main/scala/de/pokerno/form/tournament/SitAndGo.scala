package de.pokerno.form.tournament

trait SitAndGo {
  
  def minEntrantsCount: Int
  def maxEntrantsCount: Int
  
  def isSitAndGo = minEntrantsCount == maxEntrantsCount
  
}
