package de.pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import org.apache.thrift.protocol.TBinaryProtocol

object Poker {
  
  import java.nio.ByteBuffer
  import de.pokerno.protocol.thrift
  
  class Service extends thrift.rpc.Poker.FutureIface {
    import de.pokerno.poker._
    import de.pokerno.protocol.ThriftConversions._
    
    def generateDeck(): Future[ByteBuffer] = {
      val deck = new Deck
      Future.value(ByteBuffer.wrap(deck.cards))
    }
    
    def evaluateHand(cards: ByteBuffer): Future[thrift.Hand] = {
      val hand = Hand.High(cards.array()).get
      Future.value(hand)
    }
    
    def compareHands(a: Seq[Card], b: Seq[Card], board: Seq[Card]): Future[thrift.rpc.CompareResult] = {
      val h1 = Hand.High(a ++ board).get
      val h2 = Hand.High(b ++ board).get
      Future.value(
          thrift.rpc.CompareResult(h1, h2, h1 compare h2)
        )
    }
    
    def compareHands(a: ByteBuffer, b: ByteBuffer, board: ByteBuffer): Future[thrift.rpc.CompareResult] =
      compareHands(a.array(): Seq[Card], b.array(): Seq[Card], board.array(): Seq[Card])
    
    def simulateHands(a: Seq[Card], b: Seq[Card], board: Seq[Card], samples: Int): Future[thrift.rpc.SimulateResult] = {
      val h1 = Hand.High(a ++ board).get
      val h2 = Hand.High(b ++ board).get
      val hu = Math.Headsup(a, b, samples) withBoard(board)
      
      Future.value(
          thrift.rpc.SimulateResult(hu.wins, hu.ties, hu.loses)
        )
    }
    
    def simulateHands(a: ByteBuffer, b: ByteBuffer, board: ByteBuffer, samples: Int): Future[thrift.rpc.SimulateResult] =
      simulateHands(a.array(): Seq[Card], b.array(): Seq[Card], board.array(): Seq[Card], samples)
  }
  
  object Service {
    def apply(addr: java.net.InetSocketAddress) = {
      Thrift.serve[thrift.rpc.Poker.FinagledService, thrift.rpc.Poker.FutureIface](new Service, "PokerService", addr)
    }
  }

}
