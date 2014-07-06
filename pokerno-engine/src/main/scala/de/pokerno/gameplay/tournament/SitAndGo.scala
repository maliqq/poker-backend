package de.pokerno.gameplay.tournament

trait SitAndGo {
  
  def minEntrantsCount: Int
  def maxEntrantsCount: Int
  
  def isSitAndGo = minEntrantsCount == maxEntrantsCount
  
}
