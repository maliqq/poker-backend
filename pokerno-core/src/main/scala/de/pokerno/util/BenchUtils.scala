package de.pokerno.util

class BenchUtils {

  def benchmark(u: => Unit) = {
    val start = System.currentTimeMillis
    u
    val end = System.currentTimeMillis
    end - start
  }

}
