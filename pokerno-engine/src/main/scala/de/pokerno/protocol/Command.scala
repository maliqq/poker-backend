package de.pokerno.protocol

abstract class Command extends Message {
}

object CommandConversions {

  implicit def join(msg: action.JoinTable)(implicit player: Player) =
    cmd.JoinPlayer(msg.pos, player, msg.amount)

  implicit def leave(msg: action.LeaveTable)(implicit player: Player) =
    cmd.LeavePlayer(player)

  implicit def sitOut(msg: action.SitOut)(implicit player: Player) =
    cmd.SitOut(player)

  implicit def comeBack(msg: action.ComeBack)(implicit player: Player) =
    cmd.ComeBack(player)

  implicit def addBet(msg: action.AddBet)(implicit player: Player) =
    cmd.AddBet(player, msg.bet)

  implicit def discardCards(msg: action.DiscardCards)(implicit player: Player) =
    cmd.DiscardCards(player, msg.cards)

  implicit def buyIn(msg: action.BuyIn)(implicit player: Player) =
    cmd.AdvanceStack(player, msg.amount)

  implicit def rebuy(msg: action.Rebuy)(implicit player: Player) =
    cmd.Rebuy(player)

  implicit def chat(msg: action.Chat)(implicit player: Player) = cmd.Chat(player, msg.message)

  implicit def action2command(msg: PlayerEvent)(implicit player: Player): Command = msg match {
    case msg: action.JoinTable => msg
    case msg: action.LeaveTable => msg
    case msg: action.SitOut => msg
    case msg: action.ComeBack => msg
    case msg: action.AddBet => msg
    case msg: action.DiscardCards => msg
    case msg: action.BuyIn => msg
    case msg: action.Rebuy => msg
    case msg: action.Chat => msg
  }

}
