package de.pokerno.format.text

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class LexerSpec extends FunSpec with ClassicMatchers {
  describe("Lexer") {
    import Lexer.QuotedString
    
    it("quoted string") {
      new QuotedString("\"sdsdsd").unquote should equal("sdsdsd")
      new QuotedString("\"sdsdsd\"").unquote should equal("sdsdsd")
      new QuotedString("sdsdsd\"").unquote should equal("sdsdsd")
    }
  }
}
