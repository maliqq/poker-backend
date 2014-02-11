package de.pokerno.util

import org.apache.commons.codec.binary.Hex

object PrintUtils {

  def hexdump(data: Array[Byte]) = new String(Hex.encodeHex(data))

}
