package de.pokerno.gameplay.tournament

import de.pokerno.gameplay.Events
import de.pokerno.model.tournament._

trait Levels {
  
  val ctx: Context
  import ctx._
  
  val levels: List[Level]
  var currentLevelIdx: Int = 0
  def currentLevel: Level = levels(currentLevelIdx)
  
  def levelUp() {
    if (currentLevelIdx < levels.size) {
      currentLevelIdx += 1
      events broadcast Events.levelUp(currentLevelIdx, currentLevel)
    }
  }

}
