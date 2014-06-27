package de.pokerno.model.autoplay

import de.pokerno.model.AutoPlay

class Play {
  import AutoPlay._
  
  private val actions = collection.mutable.Queue[Action.Value]()
  
  def isDefined = !actions.isEmpty
  
  def clear() = actions.clear()
  
  // [x] Fold
  def willFold = actions.contains(Action.Fold)
  
  def folds() {
    if (!willFold) actions.enqueue(Action.Fold)
  }
  
  // [x] Check
  def willCheck = actions.contains(Action.Check)
  def checks() {
    if (!willCheck) actions.enqueue(Action.Check)
  }
  
  // [x] Call any
  def willCallAny = actions.contains(Action.CallAny)
  def callsAny() {
    if (!willCallAny) actions.enqueue(Action.CallAny)
  }
  
  // [x] Check/Fold
  def willCheckFold = actions.contains(Action.CheckFold)
  def checkFolds() {
    if (!willCheckFold) actions.enqueue(Action.CheckFold)
  }

}
