package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

sealed case class TournamentBuyIn(
    @Column(name = "currency_id", optionType=classOf[Long]) var currencyId: Option[Long], 
    var price: Double,
    var fee: Double,
    @Column("starting_stack") var startingStack: Int,
    @Column("addonStack") var addonStack: Int,
    @Column(name = "bounty", optionType = classOf[Double]) var bounty: Option[Double]
  ) {
  var id: Long = 0
  def this() = this(None, 0, 0, 0, 0, None)
}
