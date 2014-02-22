package de.pokerno.backend.server

import akka.actor.Actor
import de.pokerno.poker.Cards
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.Conversions._
import de.pokerno.gameplay.Notification

class Log(logdir: String, room: String) extends Actor {
  
  var writer: java.io.OutputStreamWriter = null
  
  private final val ext = ".txt"
    
  private lazy val dir = new java.io.File(logdir, room) 
  
  override def preStart() {
    dir.mkdir()
  }
  
  import proto.wire.DealType
  import proto.wire.BetSchema.BetType
  
  def receive = {
    case Notification(msg, from, to) =>
      msg match {
        case message.PlayStart(play) =>
          val id = play.id
          writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new java.io.File(dir.getPath, id + ext), true))
          
        case message.ButtonChange(pos) =>
          log("button is %d", pos)
        
        case message.StreetStart(street) =>
          log("***%s***", street)
          
        case message.BetAdd(pos, player, bet) =>
          bet.`type` match {
            case BetType.ANTE =>
              log("%s: posts ante %.2f", player, bet.amount)
            
            case BetType.SB =>
              log("%s: posts small blind %.2f", player, bet.amount)
            
            case BetType.BB =>
              log("%s: posts big blind %.2f", player, bet.amount)
            
            case BetType.CHECK =>
              log("%s: checks", player)
            
            case BetType.FOLD =>
              log("%s: folds", player)
            
            case BetType.CALL =>
              log("%s: calls %.2f", player, bet.amount)
              
            case BetType.RAISE =>
              log("%s: raises to %.2f", player, bet.amount)
            
            case BetType.BRING_IN =>
              log("%s: posts bring in %.2f", player, bet.amount)
            
            case _ =>
              log("%s", bet)
          }
          
        case message.DealCards(_type, cards, pos, player, cardsNum) =>
          _type match {
            case DealType.BOARD =>
              log("Dealt board %s", Cards(cards))
              
            case DealType.DOOR | DealType.HOLE =>
              if (cardsNum != null)
                log("Dealt %d cards to %s", cardsNum, player)
              else
                log("Dealt %s to %s", Cards(cards), player)
          }
        
        case message.DeclarePot(total, side, rake) =>
          log("Pot is %.2f", total)
        
        case message.DeclareHand(pos, player, cards, hand) =>
          log("%s shows %s (%s)", player, Cards(cards), hand.string)
          
        case message.DeclareWinner(pos, player, amount) =>
          log("%s collected %.2f from pot", player, amount)
        
        case message.PlayStop() =>
          flush()
          
        case x: Any =>
          log("unhandled: %s", x)
      }
  }
  
  override def postStop() {
    writer.close()
  }
  
  def flush() {
    writer.flush()
  }
  
  def log(s: String, args: Any*) {
    writer.write(s.format(args:_*))
    writer.write("\n")
  }
  
}
