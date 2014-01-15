package de.pokerno.backend.gateway

import akka.actor.{ Actor, ActorLogging, ActorRef }
import concurrent.duration._

object Stomp {

  object Config {
    val host = "localhost"
    val port = 1234
    val heartbeat = 1 second
  }
  
  class Server extends Actor with ActorLogging {
    override def preStart {
      
    }
    
    def receive = {
      case _ =>
    }
  }

}
/*
case class Gateway(port: Int, heartbeatInterval: Int = 1000, autoShutdown: Boolean = true) {
  import org.jboss.netty.channel.{ ChannelHandlerContext, ChannelStateEvent, SimpleChannelUpstreamHandler}
  
  import asia.stampy.common._
  import asia.stampy.common.{ heartbeat => hb }
  import asia.stampy.server.listener
  import asia.stampy.server.{ netty => server }
  
  val gw = new server.ServerNettyMessageGateway
  gw.setPort(port)
  gw.setHeartbeat(heartbeatInterval)
  gw.setAutoShutdown(autoShutdown)
  
  trait MessageListener extends gateway.StampyMessageListener {
    def setGateway(gw: gateway.AbstractStampyMessageGateway)
  }
  
  def addListener(listener: MessageListener) {
    listener.setGateway(gw)
    gw.addMessageListener(listener)
  }

  val heartbeatContainer = new hb.HeartbeatContainer

  val channelHandler = new server.ServerNettyChannelHandler
  channelHandler.setGateway(gw)
  channelHandler.setHeartbeatContainer(heartbeatContainer)

  gw.addMessageListener(new gateway.SecurityMessageListener {
    override def getMessageTypes: Array[message.StompMessageType] = Array()
    override def isForMessage(msg: message.StampyMessage[_]): Boolean = false
    override def messageReceived(msg: message.StampyMessage[_], hostPort: asia.stampy.common.gateway.HostPort) = {
    }
  })
  
  gw.addMessageListener(new listener.validate.ServerMessageValidationListener)

  gw.addMessageListener(new listener.version.VersionListener)

  val loginListener = new server.login.NettyLoginMessageListener
  loginListener.setGateway(gw)
  loginListener.setLoginHandler(new listener.login.StampyLoginHandler {
    override def login(username: String, password: String) = {}
  })
  
  gw.addMessageListener(loginListener)

  val connect = new server.connect.NettyConnectStateListener
  connect.setGateway(gw)
  gw.addMessageListener(connect)

  val heartbeat = new server.heartbeat.NettyHeartbeatListener
  heartbeat.setHeartbeatContainer(heartbeatContainer)
  heartbeat.setGateway(gw)
  gw.addMessageListener(heartbeat)

  val transaction = new server.transaction.NettyTransactionListener
  transaction.setGateway(gw)
  gw.addMessageListener(transaction)

  val sys = new listener.subscription.StampyAcknowledgementHandler {
    override def ackReceived(id: String, receipt: String, transaction: String) = {}
    override def nackReceived(id: String, receipt: String, transaction: String) = {}
    override def noAcknowledgementReceived(id: String) = {}
  }

  val ack = new server.subscription.NettyAcknowledgementListenerAndInterceptor
  ack.setHandler(sys)
  ack.setGateway(gw)
  ack.setAckTimeoutMillis(200)
  gw.addMessageListener(ack)
  gw.addOutgoingMessageInterceptor(ack)

  val receipt = new server.receipt.NettyReceiptListener
  receipt.setGateway(gw)
  gw.addMessageListener(receipt)

  val connectResponse = new server.connect.NettyConnectResponseListener
  connectResponse.setGateway(gw)
  gw.addMessageListener(connectResponse)

  gw.setHandler(channelHandler)
}
*/