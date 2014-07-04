package de.pokerno.gameplay.tournament

import de.pokerno.model._
import de.pokerno.model.tournament._
import util.Random

trait Shootout extends Rebalance {
  
  def tableSize: Int
  def entrantsCount: Int
  
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
  ------ 128/256/512/1024/2048/4096/8192/16384/32768
  
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
  
  def rebalance: Seq[List[Int]] = {
    val roundN      = Math.floor(Math.log(entrantsCount) / Math.log(tableSize)).intValue
    val roundNum = if (tableSize > 2 && roundN == 1 && entrantsCount >= tableSize * 2) { // we can build headsup tables to the final
      roundN + 1
    } else roundN
    
    val tablesCount   = Math.pow(tableSize, roundNum - 1).intValue
    println("entrantsCount=", entrantsCount, "roundNum=", roundNum,"tablesCount=",tablesCount)
    bucketize(entrantsCount, tableSize, tablesCount)
  }

}
