package de.pokerno.backend.server

import akka.actor.{ Actor, ActorLogging }

import de.pokerno.model.{Bet, DealType}
import de.pokerno.poker.Cards
import de.pokerno.gameplay.Notification
import de.pokerno.protocol.{game_events => message}

class Journal(storageDir: String, room: String) extends Actor with ActorLogging {

  var writer: java.io.OutputStreamWriter = null

  private final val ext = ".txt"

  private lazy val dir = new java.io.File(storageDir, room)

  override def preStart() {
    dir.mkdir()
  }

  def receive = {
    case Notification(msg, from, to) ⇒
      msg match {
        case message.DeclarePlayStart() ⇒
          val id = "new-uuid"//play.id
          writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new java.io.File(dir.getPath, id + ext), true))

        case message.ButtonChange(pos) ⇒
          write("button is %d", pos)

        case message.DeclareStreet(street) ⇒
          write("***%s***", street)

        case bet: message.DeclareBet ⇒
          val player = bet.player
          
          bet.ante.map { amt =>
            write("%s: posts ante %.2f", player, amt)
          } orElse
          bet.sb.map { amt =>
            write("%s: posts small blind %.2f", player, amt)
          } orElse
          bet.bb.map { amt =>
            write("%s: posts big blind %.2f", player, amt)
          } orElse
          bet.check.map { check =>
            if (check) write("%s: checks", player)
          } orElse
          bet.fold.map { fold =>
            if (fold) write("%s: folds", player)
          } orElse
          bet.call.map { amt =>
            write("%s: calls %.2f", player, amt)
          } orElse
          bet.raise.map { amt =>
            write("%s: raises to %.2f", player, amt)
          } orElse
          bet.bringIn.map { amt =>
            write("%s: posts bring in %.2f", player, amt)
          }

        case message.DealBoard(cards) ⇒
          write("Dealt board %s", cards)

        case message.DealDoor(pos, player, cardsOrCardsNum) =>
          cardsOrCardsNum match {
            case Right(cardsNum) =>
              write("Dealt door %d cards to %s", cardsNum, player)
            case Left(_) =>
          }
              
        case message.DealHole(pos, player, cardsOrCardsNum) =>
          cardsOrCardsNum match {
            case Left(cards) =>
              write("Dealt %s to %s", cards, player)
              
            case Right(cardsNum) =>
              write("Dealt %d cards to %s", cardsNum, player)
          }  

        case message.DeclarePot(total, side, rake) ⇒
          write("Pot is %.2f", total)

        case h: message.DeclareHand ⇒
          val player = h.player
          write("%s shows %s (%s)", player, h.cards, h.description)

        case message.DeclareWinner(pos, player, amount) ⇒
          write("%s collected %.2f from pot", player, amount)

        case message.DeclarePlayStop() ⇒
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
