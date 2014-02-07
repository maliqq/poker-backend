package de.pokerno.util

object PrintUtils {

  private val hexes = "0123456789abcdef".toCharArray

  def encodeHex(data: Array[Byte]): Array[String] = {
    val result = new Array[String](data.length)
    for (i ← 0 until data.length) {
      val b = data(i) & 0xff
      result(i) = new String(Array[Char](hexes(b >> 4), hexes(b & 0xf)))
    }
    result
  }

  def hexdump(data: Array[Byte]) = encodeHex(data).mkString(" ")

}
