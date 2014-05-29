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
  void close()
  void pause(1: PauseReason Reason)
  void resume()
  void cancelCurrentDeal()
}

service Deal {
  void joinPlayer(1: Player Player, 2: i32 Pos, 3: double Amount)
  void kickPlayer(1: Player Player, 2: KickReason Reason)

  void dealCards(1: wire.DealType DealType, 2: Cards Cards, 3: i32 CardsNum, 4: Player Player)
  void addBet(1: Player Player, 2: wire.Bet Bet)
  void discardCards(1: Player Player, 2: Cards Cards, 3: bool StandPat)
  void showCards(1: Player Player, 2: Cards Cards, 3: bool Muck)

  void leave(1: Player Player)
  void sitOut(1: Player Player)
  void comeBack(1: Player Player)

  void offline(1: Player Player)
  void online(1: Player Player)

  void buyIn(1: Player Player, 2: double Amount)
  void rebuy(1: Player Player, 2: double Amount)
  void doubleRebuy(1: Player Player, 2: double Amount)
  void addon(1: Player Player, 2: double Amount)
}

service Poker {
  Cards generateDeck()
  wire.Hand evaluateHand(1: Cards Cards)
  void compareHands(1: Cards A, 2: Cards B, 3: Cards Board)
  void simulateHands(1: Cards A, 2: Cards B, 3: Cards Board, 4: i64 Samples)
}
