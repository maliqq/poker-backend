package de.pokerno.model.tournament

import math.{BigDecimal => Decimal}

case class BuyIn(
  price: Decimal, // buy in price
  stack: Int, // stack for entry and re-entry (rebuy)
  fee: Option[Decimal] = None, // buy in fee
  addonStack: Option[Int] = None, // stack for add-on
  bounty: Option[Decimal] = None // price for knock out
  ) {}
