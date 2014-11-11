package de.pokerno.backend.gateway.http

import io.netty.channel._

trait AuthService {
  def authorize(secret: String): Option[String]
}

object TokenBasedAuthentication {
  final val TOKEN_PARAM = "token"
  final val TOKEN_HEADER = "X-Token-Based-Auth"
}

class TokenBasedAuthentication(authService: AuthService)
    extends AuthenticationFilter(TokenBasedAuthentication.TOKEN_PARAM, TokenBasedAuthentication.TOKEN_HEADER) {
  
  override def handleAuthParam(value: Option[String]): String = {
    if (!value.isDefined) return ""
    val player = authService.authorize(value.get)
    player.getOrElse("")
  }
  
}
