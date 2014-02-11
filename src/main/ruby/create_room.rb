#!/usr/bin/env/ruby

require 'ffi-rzmq'

ctx = ZMQ::Context.new
socket = ctx.socket ZMQ::DEALER
socket.connect("tcp://localhost:5554")

socket.send_string "11111"
