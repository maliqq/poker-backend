#!/usr/bin/env ruby
# Generated by the protocol buffer compiler. DO NOT EDIT!

require 'protocol_buffers'

begin; require 'src/main/protobuf/wire.pb'; rescue LoadError; end

module De
  module Pokerno
    module Protocol
      module Cmd
        # forward declarations
        class JoinPlayer < ::ProtocolBuffers::Message; end
        class KickPlayer < ::ProtocolBuffers::Message; end
        class Chat < ::ProtocolBuffers::Message; end
        class DealCards < ::ProtocolBuffers::Message; end
        class AddBet < ::ProtocolBuffers::Message; end
        class DiscardCards < ::ProtocolBuffers::Message; end
        class ShowCards < ::ProtocolBuffers::Message; end
        class PlayerEvent < ::ProtocolBuffers::Message; end
        class StackEvent < ::ProtocolBuffers::Message; end

        class JoinPlayer < ::ProtocolBuffers::Message
          set_fully_qualified_name "de.pokerno.protocol.cmd.JoinPlayer"

          required :int32, :Pos, 1
          required :string, :Player, 2
          required :double, :Amount, 3
        end

        class KickPlayer < ::ProtocolBuffers::Message
          set_fully_qualified_name "de.pokerno.protocol.cmd.KickPlayer"

          required :string, :Player, 1
          optional :string, :Reason, 2
        end

        class Chat < ::ProtocolBuffers::Message
          set_fully_qualified_name "de.pokerno.protocol.cmd.Chat"

          required :string, :Player, 1
          required :string, :Body, 2
        end

        class DealCards < ::ProtocolBuffers::Message
          set_fully_qualified_name "de.pokerno.protocol.cmd.DealCards"

          required ::De::Pokerno::Protocol::Wire::DealType, :Type, 1
          optional :bytes, :Cards, 2
          optional :int32, :CardsNum, 3
          optional :string, :Player, 4
        end

        class AddBet < ::ProtocolBuffers::Message
          set_fully_qualified_name "de.pokerno.protocol.cmd.AddBet"

          required :string, :Player, 1
          required ::De::Pokerno::Protocol::Wire::Bet, :Bet, 2
        end

        class DiscardCards < ::ProtocolBuffers::Message
          set_fully_qualified_name "de.pokerno.protocol.cmd.DiscardCards"

          required :string, :Player, 1
          required :bytes, :Cards, 2
        end

        class ShowCards < ::ProtocolBuffers::Message
          set_fully_qualified_name "de.pokerno.protocol.cmd.ShowCards"

          required :string, :Player, 1
          required :bytes, :Cards, 2
          optional :bool, :Muck, 3
        end

        class PlayerEvent < ::ProtocolBuffers::Message
          # forward declarations

          # enums
          module EventType
            include ::ProtocolBuffers::Enum

            set_fully_qualified_name "de.pokerno.protocol.cmd.PlayerEvent.EventType"

            LEAVE = 1
            SIT_OUT = 2
            COME_BACK = 3
            OFFLINE = 4
            ONLINE = 5
          end

          set_fully_qualified_name "de.pokerno.protocol.cmd.PlayerEvent"

          required :string, :Player, 1
          required ::De::Pokerno::Protocol::Cmd::PlayerEvent::EventType, :Type, 2
        end

        class StackEvent < ::ProtocolBuffers::Message
          # forward declarations

          # enums
          module EventType
            include ::ProtocolBuffers::Enum

            set_fully_qualified_name "de.pokerno.protocol.cmd.StackEvent.EventType"

            BUYIN = 1
            REBUY = 2
            DOUBLE_REBUY = 3
            ADDON = 4
          end

          set_fully_qualified_name "de.pokerno.protocol.cmd.StackEvent"

          required ::De::Pokerno::Protocol::Cmd::StackEvent::EventType, :Type, 1
          required :string, :Player, 2
          required :double, :Amount, 3
        end

      end
    end
  end
end
