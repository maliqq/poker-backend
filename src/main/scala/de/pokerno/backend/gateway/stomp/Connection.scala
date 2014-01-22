package de.pokerno.backend.gateway.stomp

import io.netty.channel.{Channel, ChannelFutureListener}

class Connection(channel: Channel) {

  def send(msg: String) {
    channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }
  
  import asia.stampy.common.message.{StampyMessage, StampyMessageHeader}
  import asia.stampy.client.message.ClientMessageHeader
  
  import asia.stampy.server.message.connected.ConnectedMessage
  def sendConnected = {}
  
  import asia.stampy.server.message.error.ErrorMessage
  def sendError(message: StampyMessage[StampyMessageHeader], error: String) {
    val receipt = message.getHeader.getHeaderValue(ClientMessageHeader.RECEIPT)
    val err = new ErrorMessage(receipt)
    err.getHeader.setMessageHeader(error)
    send(err.toStompMessage(true))
  }
  
  import asia.stampy.server.message.message.MessageMessage
  def sendMessage(message: StampyMessage[StampyMessageHeader], dest: String, id: String, subscription: String) = {
    val msg = new MessageMessage()
    send(msg.toStompMessage(true))
  }
  
  import asia.stampy.server.message.receipt.ReceiptMessage
  def sendReceipt = {
    val receipt = new ReceiptMessage()
    send(receipt.toStompMessage(true))
  }
  
}
