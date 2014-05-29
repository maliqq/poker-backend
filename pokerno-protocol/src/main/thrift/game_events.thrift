namespace java de.pokerno.protocol.thrift.msg

include "wire.thrift"

typedef binary Cards
typedef string Player

enum ActionEventType {
  BET_ADD = 1,
  CARDS_DISCARD = 2,
  CARDS_SHOW = 3,
}

enum SeatEventType {
  STATE = 1,
  PRESENCE = 2,
  PLAYER = 3,
  STACK_AMOUNT = 4,
  PUT_AMOUNT = 5,
}

enum TableEventType {
  BUTTON = 1,
  STATE = 2
}

enum TableEventState {
  ACTIVE = 1
}

enum GameplayEventType {
  GAME = 1,
  STAKE = 2
}

enum StageEventType {
  START = 1,
  STOP = 2,
  CANCEL = 3,
}

enum StageType {
  PLAY = 1,
  STREET = 2,
}

enum DealEventType {
  DEAL_CARDS = 1,
  REQUIRE_BET = 2,
  REQUIRE_DISCARD = 3,
  DECLARE_POT = 4,
  DECLARE_HAND = 5,
  DECLARE_WINNER = 6,
  TICK_TIMER = 7
}

enum EventType {
  SEAT = 1,
  ACTION = 2,
  TABLE = 3,
  GAMEPLAY = 4,
  STAGE = 5
}

struct AddBet {
  1: i32 Pos,
  2: Player Player

  3: wire.Bet Bet
}

struct DiscardCards {
  1: i32 Pos,
  2: Player Player,

  3: i32 CardsNum
}

struct ShowCards {
  1: i32 Pos,
  2: Player Player,

  3: Cards Cards,
  4: optional bool Muck
}

struct DealCards {
  1: wire.DealType Type,
  2: optional Cards Cards,
  3: optional i32 Pos,
  4: optional string Player,
  5: optional i32 CardsNum
}

struct RequireBet {
  1: i32 Pos,
  2: string Player,
  3: double Call,
  4: wire.MinMax Raise
}

struct RequireDiscard {
  1: i32 Pos,
  2: string Player
}

struct DeclarePot {
  1: double Pot,
  2: list<double> Side,
  3: optional double Rake
}

struct DeclareWinner {
  1: i32 Pos,
  2: string Player,
  3: double Amount
}

struct DeclareHand {
  1: i32 Pos,
  2: string Player,
  3: wire.Hand Hand,
  4: optional Cards Cards
}

struct PlayerJoin {
  1: i32 Pos,
  2: string Player,
  3: double Amount
}

struct PlayerLeave {
  1: i32 Pos,
  2: string Player
}

struct TickTimer {
  1: i32 Pos,
  2: string Player,
  3: i32 TimeLeft,
  4: optional bool TimeBank
}

struct ActionEvent {
  1: ActionEventType Type,

  2: optional AddBet BetAdd,
  3: optional DiscardCards CardsDiscard,
  4: optional ShowCards CardsShow
}

struct SeatEvent {
  1: SeatEventType Type,
  2: i32 Pos,
  3: wire.Seat Seat
}

struct TableEvent {
  1: TableEventType Type,
  2: optional i32 Button,
  3: optional TableEventState State
}

struct GameplayEvent {
  1: GameplayEventType Type,
  2: optional wire.Game Game,
  3: optional wire.Stake Stake
}

struct DealEvent {
  1: DealEventType Type,

  2: optional DealCards DealCards,
  3: optional RequireBet RequireBet,
  4: optional RequireDiscard RequireDiscard,
  5: optional DeclarePot DeclarePot,
  6: optional DeclareHand DeclareHand,
  7: optional DeclareWinner DeclareWinner,
  8: optional TickTimer TickTimer
}

struct Play {
  1: string Id,
  2: i64 StartAt,
  3: optional i64 StopAt,
  4: wire.StreetType Street,
  5: optional RequireBet Acting,
  6: optional double Pot,
  7: optional double Rake,
  8: optional Cards Board,
  9: optional Cards Pocket,
  10: map<Player, double> Winners,
  11: map<Player, Cards> KnownCards
}

struct Start {
  1: wire.Table Table,
  2: wire.Variation Variation,
  3: wire.Stake Stake,
  4: Play Play
}

struct StageEvent {
  1: StageEventType Type,
  2: StageType Stage,
  3: optional wire.StreetType Street,
  4: optional Play Play
}

struct Event {
  1: EventType Type,
  2: optional SeatEvent SeatEvent,
  3: optional ActionEvent ActionEvent,
  4: optional TableEvent TableEvent,
  5: optional GameplayEvent GameplayEvent,
  6: optional StageEvent StageEvent
}
