package de.pokerno.poker

object Bits {
  final val Flag: Byte = (1 << 6)
  
  // card is 000001 ... 110100
  // bit flag is 100001 ... 1110100
  
  def makeHidden(card: Byte): Byte = {
    (card | Flag).toByte
  }
  
  def makeVisible(card: Byte): Byte = {
    (card & ~Flag).toByte
  }
  
  def isHidden(card: Byte): Boolean = {
    (card & Flag) == Flag
  }
  
  def isVisible(card: Byte): Boolean = !isHidden(card)
  
}
