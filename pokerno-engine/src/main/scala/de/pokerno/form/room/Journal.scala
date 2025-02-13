package de.pokerno.form.room

import akka.actor.{ Actor, ActorLogging }
import de.pokerno.model.Bet
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
    case Notification(e, to, _, _) ⇒ e match {
      case msg.DeclarePlayStart(playState) ⇒
        val id = playState.play.id
        writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new java.io.File(dir.getPath, id + ext), true))

      case msg.ButtonChange(pos) ⇒
        write("button is %d", pos)

      case msg.DeclareStreet(street) ⇒
        write("***%s***", street)

      case bet: msg.DeclareBet ⇒
        val player = bet.position.player
        
        bet.action match {
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

      case msg.DealDoor(pos, cardsOrCardsNum) =>
        cardsOrCardsNum match {
          case Right(cardsNum) =>
            write("Dealt door %d cards to %s", cardsNum, pos.player)
          case Left(_) =>
        }
            
      case msg.DealHole(pos, cardsOrCardsNum) =>
        cardsOrCardsNum match {
          case Left(cards) =>
            write("Dealt %s to %s", cards, pos.player)
            
          case Right(cardsNum) =>
            write("Dealt %d cards to %s", cardsNum, pos.player)
        }  

      case msg.DeclarePot(pot, _) ⇒
        write("Pot is %.2f", pot.total)

      case msg.DeclareHand(pos, cards, hand) ⇒
        write("%s shows %s (%s)", pos.player, cards, hand.description)

      case msg.DeclareWinner(pos, amount, _) ⇒
        write("%s collected %.2f from pot", pos.player, amount)

      case msg.DeclarePlayStop() ⇒
        flush()

      case x: Any ⇒
        log.debug("unhandled: {}", x)
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
