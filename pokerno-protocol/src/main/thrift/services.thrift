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
  void createRoom(1: string id, 2: wire.Variation variation, 3: wire.Stake stake, 4: wire.Table table)
  void maintenance()
}

service Room {
  void close(1: string id)
  void pause(1: string id, 2: PauseReason Reason)
  void resume(1: string id)
  void cancelCurrentDeal(1: string id)
}

service Deal {
  void joinPlayer(1: string id, 2: Player player, 3: i32 Pos, 4: double amount)
  void kickPlayer(1: string id, 2: Player player, 3: KickReason reason)

  void dealCards(1: string id, 2: wire.DealType dealType, 3: Cards cards, 4: i32 cardsNum, 5: Player player)
  void addBet(1: string id, 2: Player player, 3: wire.Bet bet)
  void discardCards(1: string id, 2: Player player, 3: Cards cards, 4: bool standPat)
  void showCards(1: string id, 2: Player player, 3: Cards cards, 4: bool muck)

  void leave(1: string id, 2: Player player)
  void sitOut(1: string id, 2: Player player)
  void comeBack(1: string id, 2: Player player)

  void offline(1: string id, 2: Player player)
  void online(1: string id, 2: Player player)

  void buyIn(1: string id, 2: Player player, 3: double amount)
  void rebuy(1: string id, 2: Player player, 3: double amount)
  void doubleRebuy(1: string id, 2: Player player, 3: double amount)
  void addon(1: string id, 2: Player player, 3: double amount)
}

struct CompareResult {
  1: wire.Hand a
  2: wire.Hand b
  3: i32 result
}

struct SimulateResult {
  1: double wins
  2: double loses
  3: double ties
}

service Poker {
  Cards generateDeck()
  wire.Hand evaluateHand(1: Cards cards)
  CompareResult compareHands(1: Cards a, 2: Cards b, 3: Cards board)
  SimulateResult simulateHands(1: Cards a, 2: Cards b, 3: Cards board, 4: i32 samples)
}
