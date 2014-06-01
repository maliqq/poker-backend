namespace java de.pokerno.protocol.thrift.cmd

include "wire.thrift"

typedef binary Cards

enum DiscardType {
  DISCARD = 1,
  STAND_PAT = 2
}

enum ShowType {
  SHOW = 1,
  MUCK = 2
}

struct AddBet {
  1: wire.Bet Bet
}

struct DiscardCards {
  1: DiscardType Type,
  2: Cards Cards,
}

struct ShowCards {
  1: ShowType Type,
  2: optional Cards Cards
}

struct JoinTable {
  1: i32 Pos,
  2: double Amount
}

struct LeaveTable {}

struct SitOut {}

struct ComeBack {}

struct BuyIn {
  1: double Amount
}

struct Rebuy {
  1: double Amount
}

struct DoubleRebuy {
  1: double Amount
}

struct AddOn {
  1: double Amount
}
