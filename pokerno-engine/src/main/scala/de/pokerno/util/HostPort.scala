package de.pokerno.util

object HostPort {
  import java.net.InetSocketAddress
  
  implicit def string2inetSocketAddress(s: String)(implicit defaultHost: String, defaultPort: Int): InetSocketAddress = {
    if (s.indexOf(":") != -1) {
      val parts = s.split(":", 2)
      val host = if (parts(0) == "") defaultHost else parts(0)
      val port = if (parts(1) == "") defaultPort else Integer.parseInt(parts(1))
      new InetSocketAddress(host, port)
    } else {
      throw new IllegalArgumentException(f"invalid address: $s")
    }
  }
}