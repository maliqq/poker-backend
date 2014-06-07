$:.unshift("./gen-rb")
require 'poker'
require 'thrift'

def service(client_class, &block)
  socket = Thrift::Socket.new('127.0.0.1', 9091)
  transport = Thrift::FramedTransport.new(socket)
  protocol = Thrift::BinaryProtocol.new(transport)
  client = client_class.new(protocol)

  transport.open()
  block.call(client)
  transport.close()
end

def node(&block)
  require 'node'
  service(Node::Client, &block)
end

def poker(&block)
  require 'poker'
  service(Poker::Client, &block)
end
