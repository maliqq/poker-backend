require File.join(File.dirname(__FILE__), 'client.rb')

node do |client|
  client.kickPlayer(ARGV[0], ARGV[1], nil)
end
