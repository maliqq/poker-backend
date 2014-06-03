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
  1: i32 pos,
  2: Player player

  3: wire.Bet bet
}

struct DiscardCards {
  1: i32 pos,
  2: Player player,

  3: i32 cardsNum
}

struct ShowCards {
  1: i32 pos,
  2: Player player,

  3: Cards cards,
  4: optional bool muck
}

struct DealCards {
  1: wire.DealType type,
  2: optional Cards cards,
  3: optional i32 pos,
  4: optional Player player,
  5: optional i32 cardsNum
}

struct RequireBet {
  1: i32 pos,
  2: Player player,
  3: double call,
  4: wire.MinMax Raise
}

struct RequireDiscard {
  1: i32 pos,
  2: Player player
}

struct DeclarePot {
  1: double pot,
  2: list<double> side,
  3: optional double rake
}

struct DeclareWinner {
  1: i32 pos,
  2: Player player,
  3: double amount
}

struct DeclareHand {
  1: i32 pos,
  2: Player player,
  3: wire.Hand hand,
  4: optional Cards cards
}

struct PlayerJoin {
  1: i32 pos,
  2: Player player,
  3: double amount
}

struct PlayerLeave {
  1: i32 pos,
  2: Player player
}

struct TickTimer {
  1: i32 pos,
  2: Player player,
  3: i32 timeLeft,
  4: optional bool timeBank
}

struct ActionEvent {
  1: ActionEventType type,

  2: optional AddBet betAdd,
  3: optional DiscardCards cardsDiscard,
  4: optional ShowCards cardsShow
}

struct SeatEvent {
  1: SeatEventType type,
  2: i32 pos,
  3: wire.Seat seat
}

struct TableEvent {
  1: TableEventType type,
  2: optional i32 button,
  3: optional TableEventState state
}

struct GameplayEvent {
  1: GameplayEventType type,
  2: optional wire.Game game,
  3: optional wire.Stake stake
}

struct DealEvent {
  1: DealEventType type,

  2: optional DealCards dealCards,
  3: optional RequireBet requireBet,
  4: optional RequireDiscard requireDiscard,
  5: optional DeclarePot declarePot,
  6: optional DeclareHand declareHand,
  7: optional DeclareWinner declareWinner,
  8: optional TickTimer tickTimer
}

struct Play {
  1: string id,
  2: i64 started,
  3: optional i64 ended,
  4: wire.StreetType street,
  5: optional RequireBet acting,
  6: optional double pot,
  7: optional double rake,
  8: optional Cards board,
  9: optional Cards pocket,
  10: map<Player, double> winners,
  11: map<Player, Cards> pockets
}

struct Start {
  1: wire.Table table,
  2: wire.Variation variation,
  3: wire.Stake stake,
  4: Play play
}

struct StageEvent {
  1: StageEventType type,
  2: StageType stage,
  3: optional wire.StreetType street,
  4: optional Play play
}

struct Event {
  1: EventType type,
  2: optional SeatEvent seatEvent,
  3: optional ActionEvent actionEvent,
  4: optional TableEvent tableEvent,
  5: optional GameplayEvent gameplayEvent,
  6: optional StageEvent stageEvent
}
