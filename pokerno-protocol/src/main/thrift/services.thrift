namespace java de.pokerno.protocol.thrift.rpc

include "wire.thrift"

typedef string Player
typedef binary Cards

enum PauseReason {
  BREAK = 1,
  ADDON_BREAK = 2,
  BUBBLE = 3,
  PRIZE_SPLIT = 4
}

enum KickReason {}

service Node {
  void createRoom(1: string Id, 2: wire.Variation Variation, 3: wire.Stake Stake, 4: wire.Table Table)
  void maintenance()
}

service Room {
  void close(1: string Id)
  void pause(1: string Id, 2: PauseReason Reason)
  void resume(1: string Id)
  void cancelCurrentDeal(1: string Id)
}

service Deal {
  void joinPlayer(1: string Id, 2: Player Player, 3: i32 Pos, 4: double Amount)
  void kickPlayer(1: string Id, 2: Player Player, 3: KickReason Reason)

  void dealCards(1: string Id, 2: wire.DealType DealType, 3: Cards Cards, 4: i32 CardsNum, 5: Player Player)
  void addBet(1: string Id, 2: Player Player, 3: wire.Bet Bet)
  void discardCards(1: string Id, 2: Player Player, 3: Cards Cards, 4: bool StandPat)
  void showCards(1: string Id, 2: Player Player, 3: Cards Cards, 4: bool Muck)

  void leave(1: string Id, 2: Player Player)
  void sitOut(1: string Id, 2: Player Player)
  void comeBack(1: string Id, 2: Player Player)

  void offline(1: string Id, 2: Player Player)
  void online(1: string Id, 2: Player Player)

  void buyIn(1: string Id, 2: Player Player, 3: double Amount)
  void rebuy(1: string Id, 2: Player Player, 3: double Amount)
  void doubleRebuy(1: string Id, 2: Player Player, 3: double Amount)
  void addon(1: string Id, 2: Player Player, 3: double Amount)
}

struct CompareResult {
  1: wire.Hand A
  2: wire.Hand B
  3: i32 Result
}

struct SimulateResult {
  1: double Wins
  2: double Loses
  3: double Ties
}

service Poker {
  Cards generateDeck()
  wire.Hand evaluateHand(1: Cards Cards)
  CompareResult compareHands(1: Cards A, 2: Cards B, 3: Cards Board)
  SimulateResult simulateHands(1: Cards A, 2: Cards B, 3: Cards Board, 4: i32 Samples)
}
