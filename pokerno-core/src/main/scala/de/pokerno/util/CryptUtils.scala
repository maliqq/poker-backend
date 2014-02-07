package de.pokerno.util

import org.apache.commons.codec.binary.{Hex, Base64}

object CryptUtils {
  import javax.crypto.{Cipher, SecretKey}
  import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}
  import java.security.MessageDigest
  
  import javax.crypto.Mac
  
  class MessageEncryptor(secret: String) {
    private val key = generateKey(secret)
    
    import collection.JavaConversions._
    
    private def generateKey(secret: String): SecretKeySpec = {
      val digest = MessageDigest.getInstance("SHA-256")
      val hash = digest.digest(secret.getBytes("UTF-8"))
      
      new SecretKeySpec(hash, "AES")
    }
    
    def encrypt(value: String): String = {
      val aes = Cipher.getInstance("AES")
      
      val iv = new Array[Byte](16)
      util.Random.nextBytes(iv)
      
      aes.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv))
      
      Base64.encodeBase64String(
          aes.doFinal(value.getBytes("UTF-8"))
        ) + "--" + Base64.encodeBase64String(iv)
    }
    
    def encryptWithSignature(value: String): String = {
      val mac = Mac.getInstance("HmacSHA1")
      mac.init(key)
      
      encrypt(value) + "--" + Hex.encodeHexString(mac.doFinal(value.getBytes("UTF-8"))) 
    }
  }
  
  
  def encryptWithSignature(value: String, key: String) = new MessageEncryptor(key).encryptWithSignature(value)
  
}
