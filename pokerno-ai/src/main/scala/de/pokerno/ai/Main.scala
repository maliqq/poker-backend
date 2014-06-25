package de.pokerno.ai

import de.pokerno.backend._
import de.pokerno.backend.gateway._
import akka.actor.{ ActorSystem, Props }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.backend.server.{Node, Room, RoomEnv}

object Main {
  case class Config(
    // connect to postgresql database
    dbProps: Option[String] = None,
    // retrieve info from database with room id
    id: Option[String] = None,
    // or play with these params:
    tableSize: Int = 9,
    botsNum: Int = 9,
    stake: Int = 10,
    chips: Decimal = 10000.0
  )
  
  var parser = new scopt.OptionParser[Config]("poker-ai") {
    opt[Int]('t', "table-size") text("Table size from 2 to 10") action { (value, c) =>
      if (value >= 2 && value <= 10) c.copy(tableSize = value)
      else c
    }
    
    opt[Int]('t', "bots-num") text("Number of bots") action { (value, c) =>
      if (value <= c.tableSize) c.copy(botsNum = value)
      else c
    }
    
    opt[String]("id") text("Room ID") action { (value, c) =>
      c.copy(id = Some(value))
    }
    
    opt[String]("db-props") text("DB props file") action { (value, c) =>
      c.copy(dbProps = Some(value))
    }
    
    opt[Int]("stake") text("Stake BB") action { (value, c) =>
      c.copy(stake = value)
    }
    
    opt[Int]("chips") text("Stack chips") action { (value, c) =>
      c.copy(chips = value)
    }
    
    help("help") text("Help")
  }

  def main(args: Array[String]) {
    parser.parse(args, Config()) map { c =>
      val system = ActorSystem("poker-ai")
      
      val env = RoomEnv(new de.pokerno.payment.Service())
          
      val (room, game, stake) = if (c.dbProps.isDefined && c.id.isDefined) {
        // connect to db
        import de.pokerno.backend.PlayHistoryWriter
        import de.pokerno.data.pokerdb.PokerDB
        import de.pokerno.data.pokerdb.ModelConversions._
        
        val nodeEnv = Node.initDb(c.dbProps.get)(system)
        val id = java.util.UUID.fromString(c.id.get)
        val (_, _game, _, _stake) = PokerDB.Room.get(id)
        val game: Game = _game.get
        val stake: Stake = _stake
        
        Console printf("loaded from db ::: game: %s stake: %s\n", game, stake)
        
        val room = system.actorOf(Props(classOf[Room], id, game, stake, env.copy(
            pokerdb = Some(nodeEnv.db.get),
            history = Some(system.actorOf(Props(classOf[PlayHistoryWriter], nodeEnv.storage.get)))
        )), name = "poker-instance")
        
        (room, game, stake)
      } else {
        val id = java.util.UUID.randomUUID()
        val game = new Game(GameType.Texas, GameLimit.Fixed, c.tableSize)
        val stake = Stake(c.stake)
        
        val room = system.actorOf(Props(classOf[Room], id, game, stake, env), name = "poker-instance")
        
        (room, game, stake)
      }
      
      val gw = system.actorOf(Props(classOf[Http.Gateway], room, RoomGateway))
      val httpServer = new http.Server(gw,
        http.Config(port = 8081, webSocket = Right(true))
      )
      httpServer.start

      val botsNum = if (c.botsNum > game.tableSize) game.tableSize else c.botsNum
      val bots = (1 to botsNum).map { i ⇒
        system.actorOf(Props(classOf[bot.Bot],
            room,
            i - 1, // nr
            c.chips, // starting stack
            game, stake))
      }
  
      //room ! Room.Observe(gw, "http-gateway")
    }
  }
}

object RoomGateway extends Http.Events {
  
  def connect(conn: http.Connection): Http.Event.Connect = {
    Room.Connect(conn)
  }
  
  def disconnect(conn: http.Connection): Http.Event.Disconnect = {
    Room.Disconnect(conn)
  }
  
  def message(conn: http.Connection, msg: String): Http.Event.Message = {
    Console printf("!!! ignoring: %s\n", msg)
    null
  }
  
}
