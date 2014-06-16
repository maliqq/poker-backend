package de.pokerno.model.autoplay

import de.pokerno.model.AutoPlay

class Play {
  import AutoPlay._
  
  private val actions = collection.mutable.Queue[Action.Value]()
  
  def clear() = actions.clear()
  
  def willFold = actions.contains(Action.Fold)
  
  def fold() {
    if (!willFold) actions.enqueue(Action.Fold)
  }
  
  def willCheck = actions.contains(Action.Check)
  def check() {
    if (!willCheck) actions.enqueue(Action.Check)
  }
  
  def willCallAny = actions.contains(Action.CallAny)
  def callAny() {
    if (!willCallAny) actions.enqueue(Action.CallAny)
  }
  
  def willCheckFold = actions.contains(Action.CheckFold)
  def checkFold() {
    if (!willCheckFold) actions.enqueue(Action.CheckFold)
  }
}
