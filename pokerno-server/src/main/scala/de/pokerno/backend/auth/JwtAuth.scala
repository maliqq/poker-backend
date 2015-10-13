package de.pokerno.backend.auth

import de.pokerno.backend.gateway.http.AuthService
import org.jose4j.jwt
import org.jose4j.keys.HmacKey

class JwtAuth(secret: String) extends AuthService {
  def authorize(str: String): Option[String] = {
    val builder = new jwt.consumer.JwtConsumerBuilder()
    builder.setVerificationKey(new HmacKey(secret.getBytes("UTF-8")))
    val consumer = builder.build()
    try {
      val claims = consumer.processToClaims(str)
      if (claims.isClaimValueString("player")) {
    	  return Some(claims.getStringClaimValue("player"))
      }
    } catch  {
      case _: jwt.consumer.InvalidJwtException =>
    }
    None
  }
}
