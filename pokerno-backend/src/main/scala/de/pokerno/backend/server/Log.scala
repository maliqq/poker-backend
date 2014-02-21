package de.pokerno.backend.server

import akka.actor.Actor
import de.pokerno.poker.Card
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.Conversions._
import de.pokerno.gameplay.Notification

class Log extends Actor {
  var f: java.io.OutputStreamWriter = null
  
  override def preStart() {
    f = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new java.io.File("/tmp/deal.txt")))
  }
  
  import proto.wire.DealType
  import proto.wire.BetSchema.BetType
  
  def receive = {
    case Notification(msg, from, to) =>
      msg match {
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
              log("Dealt board %s", cards: List[Card])
              
            case DealType.DOOR | DealType.HOLE =>
              if (cardsNum != null)
                log("Dealt %d cards to %s", cardsNum, player)
              else
                log("Dealt to %s: %s", player, cards: List[Card])
          }
          
        case message.DeclareWinner(pos, player, amount) =>
          log("%s collected %.2f from pot", player, amount)
          
        case x: Any =>
          log("unhandled: %s", x)
      }
  }
  
  override def postStop() {
    f.close()
  }
  
  def log(s: String, args: Any*) {
    f.write(s.format(args:_*))
    f.write("\n")
  }
  
}
