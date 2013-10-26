package de.pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle.builder.{ Server, ServerBuilder }
import com.twitter.finagle
import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http._
import java.net.InetSocketAddress

import de.pokerno.backend.poker._
import de.pokerno.backend.poker.{ Math ⇒ PokerMath }

object Poker {
  class Service extends finagle.Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {
      Future.value(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
    }

    // generate deck
    case class generateDeckRq()
    case class generateDeckResp(cards: String)
    def generateDeck(r: generateDeckRq) = {
      Future.value(generateDeckResp(cards = Cards(Deck())))
    }

    // evaluate hand
    case class evaluateRq(cards: String)
    case class evaluateResp(hand: Hand)

    def evaluateHand(r: evaluateRq) = {
      val cards = Cards(r.cards)
      val hand = Hand.High(cards)

      Future value (evaluateResp(hand = hand.get))
    }

    // compare two hands
    case class compareHandsRq(a: String, b: String, board: String)
    case class compareResp(h1: Hand, h2: Hand, result: Int)

    def compareHands(r: compareHandsRq) = {
      val a = Cards(r.a)
      val b = Cards(r.b)
      val board = Cards(r.board)

      val h1 = Hand.High(a ++ board)
      val h2 = Hand.High(b ++ board)

      Future value (compareResp(h1 = h1.get, h2 = h2.get, result = h1.get compare (h2 get)))
    }

    // simulate hands
    case class simulateHandsRq(a: String, b: String, board: String, samples: Int)
    case class simulateResp(sample: PokerMath.Sample)

    def simulateHands(r: simulateHandsRq) = {
      val a = Cards(r.a)
      val b = Cards(r.b)
      val board = Cards(r.board)

      val hu = PokerMath.Headsup(a, b, r.samples)
      Future value (simulateResp(sample = hu withBoard (board)))
    }
  }

  val service: finagle.Service[HttpRequest, HttpResponse] = new Service

  object Config {
    var name = "poker_service"
    var host: String = "localhost"
    var port: Int = 8081
  }
}

class Poker extends Runnable {
  def run {
    val address = new InetSocketAddress(Node.Config.host, Node.Config.port)
    val server: Server = ServerBuilder().codec(Http()).bindTo(address).name(Node.Config.name) build (Node.service)
  }
}
