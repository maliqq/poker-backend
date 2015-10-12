package de.pokerno.backend.auth

import de.pokerno.backend.gateway.http.AuthService
import org.jose4j.jwt
import org.jose4j.keys.HmacKey

class JwtAuth(secret: String) extends AuthService {
  def authorize(str: String) = {
    val builder = new jwt.consumer.JwtConsumerBuilder()
    builder.setVerificationKey(new HmacKey(str.getBytes("UTF-8")))
    val consumer = builder.build()
    try {
      val claims = consumer.processToClaims(str)
      Some(claims.getClaimValue("player_id").asInstanceOf[String])
    } catch  {
      case _: jwt.consumer.InvalidJwtException =>
        None
    }
    
  }
}
