package de.pokerno.util

object CryptUtils {
  import javax.crypto.{Cipher, SecretKey}
  import javax.crypto.spec.SecretKeySpec
  import sun.misc.{BASE64Encoder, BASE64Decoder}
  
  import javax.crypto.Mac
  
  def encodeBase64(b: Array[Byte]) = (new BASE64Encoder).encode(b)
  
  import PrintUtils.encodeHex
  
  class MessageEncryptor(secret: String) {
    private val key = generateKey(secret)
    
    private def generateKey(secret: String): SecretKey = new SecretKeySpec(secret.getBytes, "AES")
    
    def encrypt(value: String): String = {
      val aes = Cipher.getInstance("AES/CBC/PKCS5Padding")
      aes.init(Cipher.ENCRYPT_MODE, key)
      encodeBase64(
          aes.doFinal(value.getBytes))
    }
    
    def encryptWithSignature(value: String): String = {
      val mac = Mac.getInstance("HmacSHA1")
      mac.init(key)
      
      encrypt(value) + "--" + encodeHex(mac.doFinal(value.getBytes)) 
    }
  }
  
  
  def encryptWithSignature(value: String, key: String) = new MessageEncryptor(key).encryptWithSignature(value)
  
}
