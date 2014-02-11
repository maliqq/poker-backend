package de.pokerno.backend.zmq

import scala.annotation.tailrec
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import org.zeromq.ZMQ
import akka.util.ByteString
import concurrent.duration._
import collection.immutable
import collection.mutable.ListBuffer

case class Send(frames: immutable.Seq[ByteString])
case class Request(bytes: ByteString)

object Socket {
  trait Mode
  case object DoPoll extends Mode
  case object DoPollCareful extends Mode
  case object DoFlush
  
  final val defaultPollTimeout: Duration = (100 milliseconds)
}

case class Extension(system: ActorSystem) {
  
  def socket(socketType: Int, params: Param*) = {
    
    val options = params.foldLeft(Options()) { case (opts, param) =>
      param match {
        case Connect(addr) =>
          opts.copy(connect = Some(addr))
        case Bind(addr) =>
          opts.copy(bind = Some(addr))
        case Listener(ref) =>
          opts.copy(listener = Some(ref))
        case _ =>
          opts
      }
    }
    
    system.actorOf(Props(classOf[Socket], socketType, options))
  }
}

case class Options(
    listener: Option[ActorRef] = None,
    bind: Option[String] = None,
    connect: Option[String] = None,
    subscribe: Option[String] = None
)

case class Message(frames: immutable.Seq[ByteString])

trait Param
case class Listener(ref: ActorRef) extends Param
case class Bind(addr: String) extends Param
case class Connect(addr: String) extends Param

class Socket(
    socketType: Int,
    options: Options
) extends Actor with ActorLogging {
  import Socket._
  
  val listener = options.listener
  
  val ctx = ZMQ.context(1)
  val socket = ctx.socket(socketType)
  val poller = ctx.poller()
  
  override def preStart() {
    // bind/connect
    options.bind.map { addr => socket.bind(addr) } orElse options.connect.map { addr =>
      socket.connect(addr)
    }
    
    // subscribe
    if (socketType == ZMQ.SUB) socket.subscribe(options.subscribe.getOrElse("").getBytes)
    
    // poll
    poller.register(socket, ZMQ.Poller.POLLIN)
    socketType match {
      case ZMQ.PUB | ZMQ.PUSH =>
        // skip
      case ZMQ.SUB | ZMQ.PULL | ZMQ.PAIR | ZMQ.DEALER | ZMQ.ROUTER =>
        self ! DoPoll
      case ZMQ.REQ | ZMQ.REP =>
        self ! DoPollCareful
    }
  }
  
  override def postRestart(reason: Throwable) = ()

  override def postStop() {
    if (socket != null) {
      poller.unregister(socket)
      socket.close()
    }
  }
  
  def receive = {
    case m: Mode => poll(m)
    case Send(frames) =>
      if (frames.nonEmpty) {
        val flushNow = pendingSends.isEmpty
        pendingSends.append(frames)
        if (flushNow) flush()
      }
  }

  private val pendingSends = new ListBuffer[immutable.Seq[ByteString]]

  @tailrec private def sendMessage(i: immutable.Seq[ByteString]): Boolean =
    if (i.isEmpty)
      true
    else {
      val head = i.head
      val tail = i.tail
      if (socket.send(head.toArray, if (tail.nonEmpty) ZMQ.SNDMORE else 0)) sendMessage(tail)
      else {
        pendingSends.prepend(i)
        self ! DoFlush
        false
      }
    }
  
  @tailrec private def flush() {
    if (pendingSends.nonEmpty && sendMessage(pendingSends.remove(0))) flush()
  }
 
  @tailrec def poll(mode: Mode, togo: Int = 10) {
    if (togo <= 0) self ! mode
    else receiveMessage(mode) match {
      case Seq() => pollWithTimeout(mode)
      case frames =>
        listener.map { _ ! frames }
        poll(mode, togo - 1)
    }
  }
 
  def pollWithTimeout(mode: Mode) = {
    (mode: Mode) => {
      poller.poll(defaultPollTimeout.toUnit(MILLISECONDS).toLong)
      self ! mode
    }
  }
  
  def receiveMessage(mode: Mode, currentFrames: Vector[ByteString] = Vector.empty): immutable.Seq[ByteString] = {
    if (mode == DoPollCareful && (poller.poll(0) <= 0)) {
      if (currentFrames.isEmpty)
        currentFrames
      else throw new IllegalStateException("Received partial transmission!") 
    } else {
      socket.recv(if (mode == DoPoll) ZMQ.NOBLOCK else 0) match {
        case null =>
          if (currentFrames.isEmpty) currentFrames
          else receiveMessage(mode, currentFrames)
        case bytes =>
          val frames = currentFrames :+ ByteString(bytes)
          if (socket.hasReceiveMore()) receiveMessage(mode, frames) else frames
      }
    }
  }
}
