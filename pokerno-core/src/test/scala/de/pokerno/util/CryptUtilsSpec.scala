package de.pokerno.util

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class CryptUtilsSpec extends FunSpec with ClassicMatchers {
  describe("CryptUtils") {
    val secret = "8dfd1cd74fb77044ae497cd592b8ceffead4f8b0f63952657acf99cd4b41bb5d5b594d6e643d9b2512b62952b4e5f84d8683c069b9ee3580af151eccc29bdcdf"
    val v = CryptUtils.encryptWithSignature("test", secret)
    val result = "VElYMTlJUFlid1BkWWozQ04xdzVYUT09LS15RHNDWmovS3pNdWN1UWYvSVFNYlBBPT0=--3edc2ddcd86d8f1a5b9867e1ea41388de217c931"
    v should equal(result)
  }
}
