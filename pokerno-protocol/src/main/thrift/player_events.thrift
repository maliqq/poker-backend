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
  1: wire.Bet bet
}

struct DiscardCards {
  1: DiscardType type,
  2: Cards cards,
}

struct ShowCards {
  1: ShowType type,
  2: optional Cards cards
}

struct JoinTable {
  1: i32 pos,
  2: double amount
}

struct LeaveTable {}

struct SitOut {}

struct ComeBack {}

struct BuyIn {
  1: double amount
}

struct Rebuy {
  1: double amount
}

struct DoubleRebuy {
  1: double amount
}

struct AddOn {
  1: double amount
}
