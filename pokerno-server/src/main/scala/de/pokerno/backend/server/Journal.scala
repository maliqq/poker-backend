package de.pokerno.backend.server

import akka.actor.{ Actor, ActorLogging }

import de.pokerno.model.{Bet, DealType}
import de.pokerno.poker.Cards
import de.pokerno.gameplay.Notification
import de.pokerno.protocol.msg

class Journal(storageDir: String, room: String) extends Actor with ActorLogging {

  var writer: java.io.OutputStreamWriter = null

  private final val ext = ".txt"

  private lazy val dir = new java.io.File(storageDir, room)

  override def preStart() {
    dir.mkdir()
  }

  def receive = {
    case Notification(e, from, to) ⇒ e match {
      case msg.DeclarePlayStart() ⇒
        val id = "new-uuid"//play.id
        writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new java.io.File(dir.getPath, id + ext), true))

      case msg.ButtonChange(pos) ⇒
        write("button is %d", pos)

      case msg.DeclareStreet(street) ⇒
        write("***%s***", street)

      case bet: msg.DeclareBet ⇒
        val player = bet.player
        
        bet.bet match {
          case Bet.Ante(amt) => 
            write("%s: posts ante %.2f", player, amt)
          case Bet.SmallBlind(amt) =>
            write("%s: posts small blind %.2f", player, amt)
          case Bet.BigBlind(amt) =>
            write("%s: posts big blind %.2f", player, amt)
          case Bet.Check =>
            write("%s: checks", player)
          case Bet.Fold =>
            write("%s: folds", player)
          case Bet.Call(amt) =>
            write("%s: calls %.2f", player, amt)
          case Bet.Raise(amt) =>
            write("%s: raises to %.2f", player, amt)
          case Bet.BringIn(amt) =>
            write("%s: posts bring in %.2f", player, amt)
        }

      case msg.DealBoard(cards) ⇒
        write("Dealt board %s", cards)

      case msg.DealDoor(pos, player, cardsOrCardsNum) =>
        cardsOrCardsNum match {
          case Right(cardsNum) =>
            write("Dealt door %d cards to %s", cardsNum, player)
          case Left(_) =>
        }
            
      case msg.DealHole(pos, player, cardsOrCardsNum) =>
        cardsOrCardsNum match {
          case Left(cards) =>
            write("Dealt %s to %s", cards, player)
            
          case Right(cardsNum) =>
            write("Dealt %d cards to %s", cardsNum, player)
        }  

      case msg.DeclarePot(total, side, rake) ⇒
        write("Pot is %.2f", total)

      case msg.DeclareHand(pos, player, hand) ⇒
        write("%s shows %s (%s)", player, hand.cards, hand.description)

      case msg.DeclareWinner(pos, player, amount) ⇒
        write("%s collected %.2f from pot", player, amount)

      case msg.DeclarePlayStop() ⇒
        flush()

      case x: Any ⇒
        log.info("unhandled: {}", x)
    }
  }

  override def postStop() {
    writer.close()
  }

  def flush() {
    writer.flush()
  }

  def write(s: String, args: Any*) {
    writer.write(s.format(args: _*))
    writer.write("\n")
  }

}
