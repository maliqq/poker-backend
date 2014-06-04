namespace java de.pokerno.protocol.thrift

typedef binary Cards
typedef string Player

enum DealType {
  BOARD = 1,
  HOLE = 2,
  DOOR = 3
}

enum StreetType {
  PREFLOP = 1,
  FLOP = 2,
  TURN = 3,
  RIVER = 4,

  SECOND = 5,
  THIRD = 6,
  FOURTH = 7,
  FIFTH = 8,
  SIXTH = 9,
  SEVENTH = 10,

  PREDRAW = 11,
  DRAW = 12,
  FIRST_DRAW = 13,
  SECOND_DRAW = 14,
  THIRD_DRAW = 15
}

enum RankType {
  HIGH_CARD = 1,
  ONE_PAIR = 2,
  TWO_PAIR = 3,
  THREE_KIND = 4,
  STRAIGHT = 5,
  FLUSH = 6,
  FULL_HOUSE = 7,
  FOUR_KIND = 8,
  STRAIGHT_FLUSH = 9,
  
  BADUGI_1 = 10,
  BADUGI_2 = 11,
  BADUGI_3 = 12,
  BADUGI_4 = 13,
  
  NOT_LOW = 14,
  LOW = 15
}

enum SeatState {
  EMPTY = 0,
  TAKEN = 1, // or RESERVED
  READY = 2,

  WAIT_BB = 3,
  POST_BB = 4,

  PLAY = 5,
  ALL_IN = 6,
  BET = 7,
  FOLD = 8,
  AUTO = 9, // or ZOMBIE

  IDLE = 10, // or SIT_OUT
  AWAY = 11 // or DISCONNECT
}

enum PresenceType {
  OFFLINE = 0,
  ONLINE = 1
}

enum BetType {
  ANTE = 1,
  BRING_IN = 2,
  SB = 3,
  BB = 4,
  GUEST_BLIND = 5,
  STRADDLE = 6,

  RAISE = 7,
  CALL = 8,
  CHECK = 9,
  FOLD = 10,

  ALL_IN = 11
}

enum TableState {
  WAITING = 1,
  ACTIVE = 2,
  PAUSED = 3,
  CLOSED = 4
}

enum GameType {
  TEXAS = 1,
  OMAHA = 2,
  OMAHA_8 = 3,

  STUD = 4,
  STUD_8 = 5,
  RAZZ = 6,
  LONDON = 7,

  FIVE_CARD = 8,
  SINGLE_27 = 9,
  TRIPLE_27 = 10,

  BADUGI = 11
}

enum GameLimit {
  NL = 0,
  PL = 1,
  FL = 2
}

enum MixType {
  HORSE = 1,
  EIGHT_GAME = 2
}

enum VariationType {
  GAME = 1,
  MIX = 2
}

struct MinMax {
  1: double min,
  2: double max
}

struct Seat {
  1: optional SeatState state,
  2: optional PresenceType presence,
  3: optional Player player,
  4: optional double stackAmount,
  5: optional BetType lastAction,
  6: optional double putAmount
}

struct Table {
  //int32 Size = 2;
  1: i32 button,
  2: list<Seat> seats,
  3: optional TableState state
}

struct Bet {
  1: BetType type,
  2: optional double amount,
  3: optional bool timeout
}

struct Game {
  1: GameType type,
  2: GameLimit limit,
  3: i32 tableSize
}

struct Mix {
  1: MixType type,
  2: i32 tableSize
}

struct Variation {
  1: VariationType type,
  2: optional Game game,
  3: optional Mix mix
}

struct Stake {
  1: double bigBlind,
  2: optional double smallBlind,
  3: optional double ante,
  4: optional double bringIn
}

struct Hand {
  1: RankType rank,
  2: Cards cards,
  3: Cards value,
  4: optional Cards high,
  5: optional Cards kicker,
  6: optional string description
}
