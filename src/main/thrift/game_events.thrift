namespace java de.pokerno.protocol.msg

include "wire.thrift"

typedef binary Cards
typedef string Player

enum SeatEventType {
  STATE = 1,
  PRESENCE = 2,
  PLAYER = 3,
  STACK_AMOUNT = 4,
  PUT_AMOUNT = 5,
}

struct SeatEvent {
  1: required SeatEventType Type,
  2: required i32 Pos,
  3: required wire.Seat Seat
}

struct AddBet {
  1: required wire.Bet Bet
}

enum DiscardType {
  DISCARD = 1,
  STAND_PAT = 2
}

struct DiscardCards {
  1: required DiscardType Type,

  //required i32 Pos = 2,
  //required string Player = 3,

  2: required Cards Cards,
}

enum ShowType {
  SHOW = 1,
  MUCK = 2
}

struct ShowCards {
  1: required ShowType Type,

  //required i32 Pos = 2,
  //required string Player = 3,

  2: optional Cards Cards
}

struct BetAdd {
  1: required i32 Pos,
  2: required Player Player

  3: required wire.Bet Bet
}

// PUBLISH ONLY
struct CardsDiscard {
  1: required i32 Pos,
  2: required Player Player,

  3: required i32 CardsNum
}

// PUBLISH ONLY
struct CardsShow {
  1: required i32 Pos,
  2: required Player Player,

  3: required Cards Cards,
  4: optional bool Muck
}

enum ActionEventType {
  BET_ADD = 1,
  CARDS_DISCARD = 2,
  CARDS_SHOW = 3,
}

struct ActionEvent {
  1: required ActionEventType Type,

  2: optional BetAdd BetAdd,
  3: optional CardsDiscard CardsDiscard,
  4: optional CardsShow CardsShow
}

enum TableEventType {
  BUTTON = 1,
  STATE = 2
}

enum TableEventState {
  ACTIVE = 1
}

struct TableEvent {
  1: required TableEventType Type,
  2: optional i32 Button,
  3: optional TableEventState State
}

enum GameplayEventType {
  GAME = 1,
  STAKE = 2
}

struct GameplayEvent {
  1: required GameplayEventType Type,
  2: optional wire.Game Game,
  3: optional wire.Stake Stake
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

struct DealCards {
  1: required wire.DealType Type,
  2: optional Cards Cards,
  3: optional i32 Pos,
  4: optional string Player,
  5: optional i32 CardsNum
}

struct RequireBet {
  1: required i32 Pos,
  2: required string Player,
  3: required double Call,
  4: required wire.MinMax Raise
}

struct RequireDiscard {
  1: required i32 Pos,
  2: required string Player
}

struct DeclarePot {
  1: required double Pot,
  2: list<double> Side,
  3: optional double Rake
}

struct DeclareWinner {
  1: required i32 Pos,
  2: required string Player,
  3: required double Amount
}

struct DeclareHand {
  1: required i32 Pos,
  2: required string Player,
  3: required wire.Hand Hand,
  4: optional Cards Cards
}

struct TickTimer {
  1: required i32 Pos,
  2: required string Player,
  3: required i32 TimeLeft,
  4: optional bool TimeBank
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

struct DealEvent {
  1: required DealEventType Type,

  2: optional DealCards DealCards,
  3: optional RequireBet RequireBet,
  4: optional RequireDiscard RequireDiscard,
  5: optional DeclarePot DeclarePot,
  6: optional DeclareHand DeclareHand,
  7: optional DeclareWinner DeclareWinner,
  8: optional TickTimer TickTimer
}

struct PlayerJoin {
  1: required i32 Pos,
  2: required string Player,
  3: required double Amount
}

struct PlayerLeave {
  1: required i32 Pos,
  2: required string Player
}

struct JoinTable {
  1: required i32 Pos,
  2: required double Amount
}

struct LeaveTable {
  1: optional i32 Pos
}

enum CmdType {
  JOIN_TABLE = 1,
  LEAVE_TABLE = 2,
  SIT_OUT = 3,
  COME_BACK = 4,
  ACTION = 5
}

struct Cmd {
  1: required CmdType Type,
  2: optional JoinTable JoinTable,
  3: optional ActionEvent ActionEvent
}

enum EventType {
  SEAT = 1,
  ACTION = 2,
  TABLE = 3,
  GAMEPLAY = 4,
  STAGE = 5
}

struct Play {
  1: required string Id,
  2: required i64 StartAt,
  3: optional i64 StopAt,
  4: required wire.StreetType Street,
  5: optional RequireBet Acting,
  6: optional double Pot,
  7: optional double Rake,
  8: optional Cards Board,
  9: optional Cards Pocket,
  10: map<Player, double> Winners,
  11: map<Player, Cards> KnownCards
}

struct Start {
  1: required wire.Table Table,
  2: required wire.Variation Variation,
  3: required wire.Stake Stake,
  4: required Play Play
}

struct StageEvent {
  1: required StageEventType Type,
  2: required StageType Stage,
  3: optional wire.StreetType Street,
  4: optional Play Play
}

struct Event {
  1: required EventType Type,
  2: optional SeatEvent SeatEvent,
  3: optional ActionEvent ActionEvent,
  4: optional TableEvent TableEvent,
  5: optional GameplayEvent GameplayEvent,
  6: optional StageEvent StageEvent
}
