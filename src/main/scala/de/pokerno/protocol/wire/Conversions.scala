package de.pokerno.protocol.wire

import de.pokerno.model

object Conversions {
  
  
  implicit def table2wire(t: model.Table) = new Table(1)
  
  implicit def variation2wire(v: model.Variation) = new Variation()
  
  implicit def limit2wire(l: model.Game.Limit) = GameSchema.GameLimit.NL
  
  implicit def stake2wire(s: model.Stake) = new Stake()
  
}
