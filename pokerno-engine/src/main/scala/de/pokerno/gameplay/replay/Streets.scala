package de.pokerno.gameplay.replay

import de.pokerno.model.{Street, DealType}
import de.pokerno.protocol.cmd
import de.pokerno.gameplay
import de.pokerno.gameplay.StreetStages
import de.pokerno.gameplay.Streets.streetOptions
import de.pokerno.gameplay.Stage

private[replay] object Streets {

  import gameplay.stage.impl.BringIn

  def buildStages(street: Street.Value, actions: Seq[de.pokerno.protocol.Command]) = {
    def build() = {
      val builder = new Stage.Builder[Context]()
      
      val options = streetOptions(street)
      options.dealing.map { case (dealType, cardsNum) ⇒
        builder.process("dealing") { ctx =>
          Dealing(ctx, dealType, cardsNum, actions)()
        }
      }

      if (options.bigBets) {
        // FIXME
      }

      if (options.betting) {
        builder.process("betting") { ctx =>
          Betting(ctx, actions)()
        }
      }

      new StreetStages[Context](street, builder.build())
    }

    build()
  }

}
