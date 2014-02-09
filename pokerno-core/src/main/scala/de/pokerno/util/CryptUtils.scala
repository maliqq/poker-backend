package de.pokerno.util

import org.apache.commons.codec.binary.{Hex, Base64}

object CryptUtils {
  import javax.crypto.{Cipher, SecretKey}
  import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}
  import java.security.MessageDigest
  
  import javax.crypto.Mac
  
  class MessageEncryptor(secret: String) {
    private val key = generateKey(secret)
    
    lazy val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    lazy val mac = Mac.getInstance("HmacSHA1")
  
    final val delim = "--"
    
    import collection.JavaConversions._
    
    private def generateKey(secret: String): SecretKeySpec = {
      val digest = MessageDigest.getInstance("SHA-256")
      val hash = digest.digest(secret.getBytes("UTF-8"))
      
      new SecretKeySpec(hash, "AES")
    }
    
    def verify(value: String): Array[Byte] = {
      val Array(data, digest) = value.split(delim, 2)
      if (digest == generateDigest(data)) Base64.decodeBase64(data)
      else null // TODO exception
    }
    
    def decrypt_and_verify(value: String) = {
      val data = verify(value)
      decrypt(new String(data))
    }
    
    def encrypt(value: String): String = {
      val iv = new Array[Byte](16)
      util.Random.nextBytes(iv)
      
      cipher.init(Cipher.ENCRYPT_MODE,
          key,
          new IvParameterSpec(iv))
      
      Base64.encodeBase64String(
          cipher.doFinal(value.getBytes("UTF-8"))
        ) + delim + Base64.encodeBase64String(iv)
    }
    
    def decrypt(value: String) = {
      val Array(msg, iv, _*) = value.split(delim, 2)
      cipher.init(Cipher.DECRYPT_MODE,
          key,
          new IvParameterSpec(Base64.decodeBase64(iv)))
      cipher.update(Base64.decodeBase64(msg))
      cipher.doFinal()
    }
    
    def encryptWithSignature(value: String): String = {
      Base64.encodeBase64String(encrypt(value).getBytes) + delim + generateDigest(value) 
    }
    
    def generateDigest(value: String) = {
      mac.init(key)
      
      Hex.encodeHexString(mac.doFinal(value.getBytes("UTF-8")))
    }
  }
  
  
  def encryptWithSignature(value: String, key: String) = new MessageEncryptor(key).encryptWithSignature(value)
  
}
