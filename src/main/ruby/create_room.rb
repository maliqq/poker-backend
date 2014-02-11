#!/usr/bin/env/ruby

$:.unshift(File.dirname(__FILE__))
$:.unshift(File.join(File.dirname(__FILE__), 'protobuf'))

require 'ffi-rzmq'
require 'proto/rpc.pb'

ctx = ZMQ::Context.new
socket = ctx.socket ZMQ::DEALER
socket.connect("tcp://0.0.0.0:5554")

#table = Wire::Table.new()
stake = Wire::Stake.new(
  BigBlind: 100,
  SmallBlind: 50
  )
variation = Wire::Variation.new(
  Type: Wire::Variation::VariationType::GAME,
  Game: Wire::Game.new(
    Type: Wire::Game::GameType::TEXAS,
    Limit: Wire::Game::GameLimit::NL,
    TableSize: 9
    )
  )

msg = Rpc::Request.new(
  Type: Rpc::Request::RequestType::NODE_ACTION,
  NodeAction: Rpc::NodeAction.new(
    Type: Rpc::NodeAction::ActionType::CREATE_ROOM,
    CreateRoom: Rpc::CreateRoom.new(
      Id: "test-1",
      #Table: table,
      Variation: variation,
      Stake: stake
      )
    )
  )

socket.send_string msg.serialize_to_string
socket.close
