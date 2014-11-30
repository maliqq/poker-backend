package de.pokerno.backend.server.node

import akka.actor.{Actor, ActorSystem, ActorRef, Props}

trait Initialize extends init.Database { a: Actor =>
  import context._
  import de.pokerno.form.Room
  import Room.Topics
  
  def sessionConnector: Option[()=>org.squeryl.Session]
  def nodeId: java.util.UUID
  def topicBroadcasts: Map[String, List[de.pokerno.backend.server.Broadcast]]
  
  private var _metrics: MetricsHandler = _
  lazy val metrics: MetricsHandler = _metrics
  
  protected val notificationConsumers = collection.mutable.ListBuffer[ActorRef]()
  protected val topicConsumers = collection.mutable.Map[String, List[ActorRef]]().withDefaultValue(Nil)
  
  if (sessionConnector.isDefined) {
    buildConnectionContext(sessionConnector.get)
    
    val storage = buildStorage
    val pokerdb = buildDatabaseService
    
    notificationConsumers += actorOf(Props(
        new Actor {
          import de.pokerno.gameplay.Notification
          import de.pokerno.protocol.msg
          import de.pokerno.data.pokerdb.thrift.{State => ThriftState}
          
          def receive = {
            case Notification(payload, roomId, _, _) =>
              payload match {
                case msg.PlayerJoin(pos, amount) =>
                  pokerdb.startSession(roomId, pos.player, pos.pos, amount.toDouble)
                  
                case msg.PlayerLeave(pos) => // TODO tell how much money left
                  pokerdb.endSession(roomId, pos.player, pos.pos, 0) // FIXME amount?
                
                case _ => // ignore
              }
            
            case Room.ChangedState(id, newState) =>
              pokerdb.changeRoomState(id, ThriftState.valueOf(newState.toString().toLowerCase).get)
          }
        }
      ))

    notificationConsumers += actorOf(Props(
        new Actor {
          import de.pokerno.model
          
          def receive = {
            case (id: java.util.UUID, game: model.Game, stake: model.Stake, play: model.Play) =>
              //log.info("writing {} {}", id, play)
              storage.write(id, game, stake, play)
          }
        }
      ))
    
    val roomMetrics = actorOf(Props(
        new Actor {
          def receive = {
            case Room.Metrics(id, metrics) =>
              pokerdb.reportRoomMetrics(id, metrics)
          }
        }
      ))
    topicConsumers(Topics.Metrics) :+= roomMetrics
    
    _metrics = new MetricsHandler {
      def report() {
        pokerdb.reportNodeMetrics(nodeId.toString(), this.metrics)
      }
    }
  } else {
    _metrics = new MetricsHandler {
      def report() {
        Console printf("metrics: %s\n", this.metrics)
      }
    }
  }
  
  for ((topic, broadcasts) <- topicBroadcasts) {
    for (broadcast <- broadcasts) {
      val consumer = actorOf(Props(
        new Actor {
          def receive = {
            case Room.Created(id) =>
              broadcast.broadcast(topic, "{\"type\":\"created\",\"id\":\"{}\"}".format(id))
            case Room.ChangedState(id, state) =>
              // TODO
            case Room.Metrics(id, metrics) =>
              // TODO
          }
        }
      ))

      topicConsumers(topic) :+= consumer
    }
  }
}
