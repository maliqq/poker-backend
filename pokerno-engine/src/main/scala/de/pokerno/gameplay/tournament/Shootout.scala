package de.pokerno.gameplay.tournament

trait Shootout {
  
  def tableSize: Int
  
  /*
  Headsup scheme:
  
              [Final table]
                /        \
           [Leaf]        [Leaf]
          /     \         /    \
      [Leaf]   [Leaf]  [Leaf]    [Leaf]
  
  ------ 4 players - 2 rounds
  ------ 8 players - 3 rounds
  ------ 16 players - 4 rounds
  ------ 32 players - 5 rounds
  ------ 64 players - 6 rounds
  
  Double shootout scheme:
          [Final table: X players]
                 ||||||
          [First round X tables]
  ------- 6-max: 6 tables (36 players)
  ------- 9-max: 9 tables (81 players)
  
  Triple shootout scheme:
          [Final table: X players]
                 ||||||
          [Second round X tables]
                 ||||||
          [First round X tables]
  ------- 6-max: 36 tables first round (216 players), 6 tables second round (36 players)
  ------- 9-max: 81 tables first round (729 players), 9 tables second round (81 players)
  ------- 10-max: 100 tables first round (1000 players), 10 tables second round (100 players)
  
  Неравномерное количество участников:
  - хедзап: автоматический переход в следующий раунд либо возврат байина
  - неполные столы
  */

}
