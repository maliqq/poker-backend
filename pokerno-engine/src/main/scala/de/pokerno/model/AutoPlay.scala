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
  
}
