namespace java de.pokerno.finance.thrift

typedef string Player

exception Error {
  1: string Message
}

service Balance {
  double available(1: Player player)
  double inPlay(1: Player player)
  
  void deposit(1: Player player, 2: double amount) throws (1: Error error)
  void withdraw(1: Player player, 2: double amount) throws (1: Error error)
}
