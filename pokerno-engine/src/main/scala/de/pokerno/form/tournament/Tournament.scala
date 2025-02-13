package de.pokerno.form.tournament

import akka.actor.{Actor, ActorLogging, FSM}

object Tournament {
  case class Register(player: Player)
  case class Rebuy(player: Player)
  case class Addon(player: Player)
  case class Knockout(winner: Player, looser: Player)
  case class Eliminate(player: Player)
  
  trait State
  case object Waiting extends State
  case object WaitingShootoutRoundCompletion extends State
  case object Active extends State
  case object Bubble extends State
  trait Data
}

class Tournament extends Actor with FSM[Tournament.State, Tournament.Data] {
  import Tournament._
  
  override def preStart {
  }
  
  /*
  == Фаза ожидания
  Условия начала турнира:
  - достигнуто нужное количество зарегистрированных игроков
  - турнир запущен по таймеру, есть гарантированная сумма выплат
  Условия отмены турнира:
  - турнир запущен по таймеру, не достигнуто минимальное количество игроков
  Принимает следующие типы сообщений:
  - вход на турнир
  - отмена входа и возврат взноса
  Раздает следующие типы сообщений:
  - анонс старта турнира
  - турнир отменен
  - рассадка игроков
  */
  when(Waiting) {
    case Event(_, _) =>
      stay()
  }
  
  /*
  == Активная фаза турнира
  Принимает следующие типы сообщений:
  - в случае поздней регистрации - входы новых игроков на турнир без возможности возврата средств
  - вылеты игроков из турнира
    - при возможности докупиться - запрашивать докупку
  Раздает следующие типы сообщений:
  - анонс перерывов
  - анонс перерыва с аддоном
  - баббл
  - рассадка игроков
  */
  when(Active) {
    case Event(_, _) =>
      stay()
  }
  
  /*
  Баббл - синхронный старт всех раздач за столом
  */
  when(Bubble) {
    case Event(_, _) =>
      stay()
  }
  
  /*
  Ждем окончания раунда в шутаут-турнире
  */
  when(WaitingShootoutRoundCompletion) {
    case Event(_, _) =>
      stay()
  }
  
  whenUnhandled {
    case _ =>
      stay()
  }
  
  override def postStop {
    
  }
  
}