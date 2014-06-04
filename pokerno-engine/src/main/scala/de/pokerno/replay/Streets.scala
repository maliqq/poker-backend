package de.pokerno.replay

import de.pokerno.model.{Street, DealType}
import de.pokerno.protocol.cmd
import de.pokerno.gameplay
import de.pokerno.gameplay.StreetStageChain
import de.pokerno.gameplay.Streets.streetOptions

private[replay] object Streets {

  def buildStages(street: Street.Value, actions: Seq[de.pokerno.protocol.Command]) = {
    def build() = {
      import gameplay.Stages.{process, stage}
      import gameplay.stages.BringIn

      val stages = new StreetStageChain(street)
      
      val options = streetOptions(street)
      options.dealing.map { case (dealType, cardsNum) ⇒
        val dealActions = actions.filter { action ⇒
          action match {
            case a: cmd.DealCards ⇒   a._type == dealType
            case _ ⇒                  false
          }
        }.asInstanceOf[List[cmd.DealCards]]

        stages ~> process("dealing") { ctx =>
          //Dealing(ctx, dealActions, dealType, cardsNum)()
        }
      }

      if (options.bigBets) {
        // FIXME
      }

      if (options.betting) {
        val betActions = actions.filter(_.isInstanceOf[cmd.AddBet]).asInstanceOf[List[cmd.AddBet]]

        stages ~> process("betting") { ctx =>
          //Betting(ctx, betActions)()
        }
      }

      stages
    }

    build()
  }

}
