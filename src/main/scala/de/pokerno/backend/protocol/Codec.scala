package de.pokerno.backend.protocol

import scala.collection.mutable.{ Map => MMap }

abstract class Codec {
//  def encode(msg: Message): Array[Byte]
//  def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T
}

object Codec {
  object MsgPack extends Codec {
  }
  object Json extends Codec {
    
  }
  object Protobuf extends Codec {}
  
  private val registry: MMap[String, Class[Message]] = MMap.empty
  
  def register[T <: Message](name: String, _type: Class[T]) {
    registry(name) = _type.asInstanceOf[Class[Message]]
  }
  def lookup(name: String) = registry(name)
  
  register("acting", classOf[Message.Acting])
  register("add_bet", classOf[Message.AddBet])
  register("change_game", classOf[Message.ChangeGame])
  register("chat_message", classOf[Message.ChatMessage])
  register("collect_pot", classOf[Message.CollectPot])
  register("come_back", classOf[Message.ComeBack])
  register("deal_cards", classOf[Message.DealCards])
  register("dealer_message", classOf[Message.DealerMessage])
  register("discard_cards", classOf[Message.DiscardCards])
  register("discarded", classOf[Message.Discarded])
  register("error_message", classOf[Message.ErrorMessage])
  register("join_table", classOf[Message.JoinTable])
  register("kick_player", classOf[Message.KickPlayer])
  register("leave_table", classOf[Message.LeaveTable])
  register("move_button", classOf[Message.MoveButton])
  register("play_start", classOf[Message.PlayStart])
  register("play_stop", classOf[Message.PlayStop])
  register("require_bet", classOf[Message.RequireBet])
  register("require_discard", classOf[Message.RequireDiscard])
  register("seat_state_change", classOf[Message.SeatStateChange])
  register("show_cards", classOf[Message.ShowCards])
  register("show_hand", classOf[Message.ShowHand])
  register("sit_out", classOf[Message.SitOut])
  register("street_start", classOf[Message.StreetStart])
  register("winner", classOf[Message.Winner])
}
