package de.pokerno.format.text

import org.scalatest._
import org.scalatest.Matchers._

class LexerSpec extends FunSpec {
  describe("Lexer") {
    import Lexer.QuotedString

    it("quoted string") {
      new QuotedString("\"sdsdsd").unquote should equal("sdsdsd")
      new QuotedString("\"sdsdsd\"").unquote should equal("sdsdsd")
      new QuotedString("sdsdsd\"").unquote should equal("sdsdsd")
    }
  }
}
