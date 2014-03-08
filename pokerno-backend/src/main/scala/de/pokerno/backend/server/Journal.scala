package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging}
import de.pokerno.poker.Cards
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.CommonConversions._
import de.pokerno.protocol.Conversions._
import de.pokerno.gameplay.Notification

class Journal(storageDir: String, room: String) extends Actor with ActorLogging {
  
  var writer: java.io.OutputStreamWriter = null
  
  private final val ext = ".txt"
    
  private lazy val dir = new java.io.File(storageDir, room) 
  
  override def preStart() {
    dir.mkdir()
  }
  
  import proto.wire.DealType
  import proto.wire.BetType
  
  def receive = {
    case Notification(msg, from, to) =>
      msg match {
        case message.PlayStart(play) =>
          val id = play.id
          writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new java.io.File(dir.getPath, id + ext), true))
          
        case message.ButtonChange(pos) =>
          write("button is %d", pos)
        
        case message.StreetStart(street) =>
          write("***%s***", street)
          
        case message.BetAdd(pos, player, bet) =>
          bet.`type` match {
            case BetType.ANTE =>
              write("%s: posts ante %.2f", player, bet.amount)
            
            case BetType.SB =>
              write("%s: posts small blind %.2f", player, bet.amount)
            
            case BetType.BB =>
              write("%s: posts big blind %.2f", player, bet.amount)
            
            case BetType.CHECK =>
              write("%s: checks", player)
            
            case BetType.FOLD =>
              write("%s: folds", player)
            
            case BetType.CALL =>
              write("%s: calls %.2f", player, bet.amount)
              
            case BetType.RAISE =>
              write("%s: raises to %.2f", player, bet.amount)
            
            case BetType.BRING_IN =>
              write("%s: posts bring in %.2f", player, bet.amount)
            
            case _ =>
              write("%s", bet)
          }
          
        case message.DealCards(_type, cards, pos, player, cardsNum) =>
          _type match {
            case DealType.BOARD =>
              write("Dealt board %s", Cards(cards))
              
            case DealType.DOOR | DealType.HOLE =>
              if (cardsNum != null)
                write("Dealt %d cards to %s", cardsNum, player)
              else
                write("Dealt %s to %s", Cards(cards), player)
          }
        
        case message.DeclarePot(total, side, rake) =>
          write("Pot is %.2f", total)
        
        case message.DeclareHand(pos, player, cards, hand) =>
          write("%s shows %s (%s)", player, Cards(cards), hand.string)
          
        case message.DeclareWinner(pos, player, amount) =>
          write("%s collected %.2f from pot", player, amount)
        
        case message.PlayStop() =>
          flush()
          
        case x: Any =>
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
    writer.write(s.format(args:_*))
    writer.write("\n")
  }
  
}
