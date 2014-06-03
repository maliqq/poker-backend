$:.unshift("./gen-rb")
require 'poker'
require 'thrift'

socket = Thrift::Socket.new('localhost', 9091)
transport = Thrift::FramedTransport.new(socket)
protocol = Thrift::BinaryProtocol.new(transport)
client = Poker::Client.new(protocol)

transport.open()

kinds = "23456789TJQKA"
suits = "♠♥♦♣"
puts "generateDeck() = " + client.generateDeck().bytes.map { |byte|
  n = byte - 1
  kind = n >> 2
  suit = n % 4
  kinds[kind] + suits[suit]
}.join("")

transport.close()
