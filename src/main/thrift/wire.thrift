namespace java de.pokerno.protocol

typedef binary Cards

enum DealType {
  BOARD = 1,
  HOLE = 2,
  DOOR = 3
}

struct MinMax {
  1: double Min,
  2: double Max
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

struct Box {
  1: required i32 Pos,
  2: required string Player
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

struct Seat {
  1: optional SeatState State,
  2: optional PresenceType Presence,
  3: optional string Player,
  4: optional double StackAmount,
  5: optional BetType LastAction,
  6: optional double PutAmount
}

enum TableState {
  WAITING = 1,
  ACTIVE = 2,
  PAUSED = 3,
  CLOSED = 4
}

struct Table {
  //required int32 Size = 2;
  1: required i32 Button,
  2: list<Seat> Seats,
  3: optional TableState State
}

struct Bet {
  1: required BetType Type,
  2: optional double Amount,
  3: optional bool Timeout
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

struct Game {
  1: required GameType Type,
  2: required GameLimit Limit,
  3: required i32 TableSize
}

enum MixType {
  HORSE = 1,
  EIGHT_GAME = 2
}

struct Mix {
  1: required MixType Type,
  2: required i32 TableSize
}

enum VariationType {
  GAME = 1,
  MIX = 2
}

struct Variation {
  1: required VariationType Type,
  2: optional Game Game,
  3: optional Mix Mix
}

struct Stake {
  1: required double BigBlind,
  2: optional double SmallBlind,
  3: optional double Ante,
  4: optional double BringIn
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
  
  BADUGI1 = 10,
  BADUGI2 = 11,
  BADUGI3 = 12,
  BADUGI4 = 13,
  
  NOT_LOW = 14,
  LOW = 15
}

struct Hand {
  1: required RankType Rank,
  2: required Cards Cards,
  3: required Cards Value,
  4: optional Cards High,
  5: optional Cards Kicker,
  6: optional string String
}
