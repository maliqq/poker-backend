require 'ffi-rzmq'

context = ZMQ::Context.new
socket = context.socket ZMQ::SUB
puts "subscribing to #{ARGV[0].inspect}"
socket.setsockopt(ZMQ::SUBSCRIBE, "test-badugi")
socket.connect("tcp://127.0.0.1:5555")

while true
  topic = ""
  socket.recv_string(topic)

  msg = ""
  socket.recv_string(msg)

  puts msg
end
