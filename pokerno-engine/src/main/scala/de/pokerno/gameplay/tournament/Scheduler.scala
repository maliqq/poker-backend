package de.pokerno.gameplay.tournament

import akka.actor.{Actor, ActorLogging}
import concurrent.duration._

object Scheduler {
  trait Break { a => Actor
  }
  
  trait LevelUp { a => Actor
  }
  
  trait BubblePause { a => Actor
  }   
}

/*

Следит за временными отрезками стадий турнира

Общая схема:

Турнир анонсировали <--- Время ранней регистрации ---><-- Рассадка за столы --><-- Уровни блайндов --><-- Перерывы --><-- Баббл -->

События:
* турнир стартовал
* турнир на паузе (перерыв)
* турнир на паузе (перерыв для дозакупок)
* завершен период поздней регистрации
* завершен период дозакупок и повторных входов
* баббл
*/

class Scheduler extends Actor {
  
  import context._
  import Scheduler._
  
  override def preStart {
    
  }
  
  def receive = {
    case _ =>
  }
  
  override def postStop {
    
  }
  
}
