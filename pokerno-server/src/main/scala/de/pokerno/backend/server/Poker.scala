package de.pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle.builder.{ Server, ServerBuilder }
import com.twitter.finagle
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._
import java.net.InetSocketAddress

import de.pokerno.poker
import de.pokerno.poker.{ Math ⇒ PokerMath }

object Poker {
  class Service extends finagle.Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {
      Future.value(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
    }

    // generate deck
    case class generateDeckRq()
    case class generateDeckResp(cards: String)
    def generateDeck(r: generateDeckRq) = {
      Future.value(generateDeckResp(cards = poker.Cards(poker.Deck())))
    }

    // evaluate hand
    case class evaluateRq(cards: String)
    case class evaluateResp(hand: poker.Hand)

    def evaluateHand(r: evaluateRq) = {
      val cards = poker.Cards(r.cards)
      val hand = poker.Hand.High(cards)

      Future value (evaluateResp(hand = hand.get))
    }

    // compare two hands
    case class compareHandsRq(a: String, b: String, board: String)
    case class compareResp(h1: poker.Hand, h2: poker.Hand, result: Int)

    def compareHands(r: compareHandsRq) = {
      val a = poker.Cards(r.a)
      val b = poker.Cards(r.b)
      val board = poker.Cards(r.board)

      val h1 = poker.Hand.High(a ++ board)
      val h2 = poker.Hand.High(b ++ board)

      Future value (compareResp(h1 = h1.get, h2 = h2.get, result = h1.get compare (h2 get)))
    }

    // simulate hands
    case class simulateHandsRq(a: String, b: String, board: String, samples: Int)
    case class simulateResp(sample: PokerMath.Sample)

    def simulateHands(r: simulateHandsRq) = {
      val a = poker.Cards(r.a)
      val b = poker.Cards(r.b)
      val board = poker.Cards(r.board)

      val hu = PokerMath.Headsup(a, b, r.samples)
      Future value (simulateResp(sample = hu withBoard (board)))
    }
  }
}

class Poker extends Runnable {
  def run {
  }
}
