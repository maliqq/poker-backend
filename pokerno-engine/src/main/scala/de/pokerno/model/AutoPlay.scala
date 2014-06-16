package de.pokerno.model

import com.fasterxml.jackson.annotation.JsonValue

object AutoPlay {
  
  object Action extends Enumeration {
    private def action(name: String) = new Val(nextId, name)
    
    //final val SitOut      = action("sit-out")
    final val Fold        = action("fold")
    final val Check       = action("check")
    final val CallAny     = action("call-any")
    final val CheckFold   = action("check-fold")
    final val FoldAnyBet  = action("fold-any-bet")
  }
  
  class Play {
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
  
  class SitOut {
    
    private var _willSitOut = false
    
    def willSitOut = _willSitOut
    
    def sitOut() {
      if (!willSitOut) _willSitOut = true
    }
    
    def toggle() {
      _willSitOut = !willSitOut
    }
    
  }
  
}
