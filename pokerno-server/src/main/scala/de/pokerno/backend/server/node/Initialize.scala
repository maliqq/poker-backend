package de.pokerno.backend.server.node

import akka.actor.{Actor, ActorSystem, ActorRef, Props}

trait Initialize extends init.Database { a: Actor =>
  import context._
  import de.pokerno.form.Room
  import Room.Topics
  
  def sessionConnector: Option[()=>org.squeryl.Session]
  def nodeId: java.util.UUID
  def broadcastTopics: List[Tuple2[de.pokerno.backend.server.Broadcast, List[String]]]
  
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
            
            // FIXME dead code
            case Room.ChangedState(id, newState) =>
              pokerdb.changeRoomState(id, ThriftState.valueOf(newState.toString().toLowerCase).get)
          }
        }
      ))

    // room history
    val roomHistory = actorOf(Props(
        new Actor {
          import de.pokerno.gameplay
          
          def receive = {
            case gameplay.Deal.Dump(id, game, stake, play) =>
              //log.info("writing {} {}", id, play)
              storage.write(id, game, stake, play)
          }
        }
      ))
    topicConsumers(Topics.Deals) :+= roomHistory
    
    // room metrics reporter
    val roomMetrics = actorOf(Props(
        new Actor {
          def receive = {
            case Room.Metrics(id, metrics) =>
              pokerdb.reportRoomMetrics(id, metrics)
          }
        }
      ))
    topicConsumers(Topics.Metrics) :+= roomMetrics
    
    // node metrics
    _metrics = new MetricsHandler {
      def report() {
        pokerdb.reportNodeMetrics(nodeId.toString(), this.metrics)
      }
    }
  } else {
    // node metrics
    _metrics = new MetricsHandler {
      def report() {
        Console printf("metrics: %s\n", this.metrics)
      }
    }
  }
  
  for (broadcastTopic <- broadcastTopics) {
    val Tuple2(bcast, topics) = broadcastTopic
    
    val consumer = actorOf(Props(
      new Actor {
        final val mapper = new com.fasterxml.jackson.databind.ObjectMapper()

        def receive = {
          case Room.Created(id) =>
            bcast.broadcast("room.state",
              """{"type":"created","id":"{}"}""".format(id))
          
          case Room.ChangedState(id, state) =>
            // TODO
          
          case Room.Metrics(id, metrics) => 
            bcast.broadcast("room.state",
              """{"type":"updated","id":"%s","payload":%s""".format(id, mapper.writeValueAsString(metrics)))
        }
      }
    ))

    for (topic <- topics) {
      topicConsumers(topic) :+= consumer
    }
  }
}
