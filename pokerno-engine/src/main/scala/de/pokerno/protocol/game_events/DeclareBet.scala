package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.{Bet, BetType}
import com.fasterxml.jackson.annotation.JsonInclude

object DeclareBet {
  def apply(pos: Int, player: Player, bet: Bet): DeclareBet = bet.betType match {
    case BetType.Fold =>
      new DeclareBet(pos, player, fold = Some(true))
    case BetType.Check =>
      new DeclareBet(pos, player, check = Some(true))
    case BetType.Call =>
      new DeclareBet(pos, player, call = Some(bet.amount.get))
    case BetType.Raise =>
      new DeclareBet(pos, player, raise = Some(bet.amount.get))
    case BetType.SmallBlind =>
      new DeclareBet(pos, player, sb = Some(bet.amount.get))
    case BetType.BigBlind =>
      new DeclareBet(pos, player, bb = Some(bet.amount.get))
    case BetType.Ante =>
      new DeclareBet(pos, player, ante = Some(bet.amount.get))
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class DeclareBet(
    @BeanProperty val pos: Int,
    
    @BeanProperty val player: Player,
    
    @BeanProperty val check: Option[Boolean] = None,
    
    @BeanProperty val fold: Option[Boolean] = None,
    
    @BeanProperty val call: Option[Decimal] = None,
    
    @BeanProperty val sb: Option[Decimal] = None,
    
    @BeanProperty val bb: Option[Decimal] = None,
    
    @BeanProperty val raise: Option[Decimal] = None,
    
    @BeanProperty val ante: Option[Decimal] = None,
    
    @BeanProperty val bringIn: Option[Decimal] = None
  ) extends GameEvent {
  @BeanProperty var timeout: Option[Boolean] = None
}

// object DeclareBet {
//   def apply(pos: Int, player: Player, bet: Bet) = bet.betType match {
//     case Bet.Fold =>        new DeclareFold(pos, player, bet)
//     case Bet.Check =>       new DeclareCheck(pos, player, bet)
//     case Bet.Call =>        new DeclareCall(pos, player, bet)
//     case Bet.Raise =>       new DeclareRaise(pos, player, bet)
//     case Bet.BringIn =>     new DeclareBringIn(pos, player, bet)
//     case Bet.Ante =>        new DeclareAnte(pos, player, bet)
//     case Bet.SmallBlind =>  new DeclareSmallBlind(pos, player, bet)
//     case Bet.BigBlind =>    new DeclareBigBlind(pos, player, bet)
//     case Bet.GuestBlind =>  new DeclareGuestBlind(pos, player, bet)
//     case Bet.Straddle  =>   new DeclareStraddle(pos, player, bet)
//     case _ =>
//       throw new Exception("unknown bet to serialize: %s" format bet)
//   }
// }

// @JsonInclude(JsonInclude.Include.NON_NULL)
// abstract class DeclareBet(
//     @BeanProperty val pos: Int,
    
//     @BeanProperty val player: Player,
    
//     @BeanProperty val amount: Option[Decimal] = None
//   ) extends GameEvent {}

// sealed class DeclareRaise(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)

// sealed class DeclareAnte(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)

// sealed class DeclareBringIn(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)

// sealed class DeclareSmallBlind(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)

// sealed class DeclareBigBlind(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)

// sealed class DeclareGuestBlind(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)

// sealed class DeclareStraddle(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)

// sealed class DeclareCheck(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, None)

// sealed class DeclareFold(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, None)

// sealed class DeclareCall(
//     _pos: Int, _player: Player, _bet: Bet
// ) extends DeclareBet(_pos, _player, _bet.amount)
