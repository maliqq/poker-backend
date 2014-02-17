protostuff:
	java -jar ~/Downloads/protostuff-compiler-1.0.7-jarjar.jar project/protostuff.properties
protoc-ruby:
	protoc --ruby_out=src/main/ruby/proto --proto_path=src/main/protobuf/ src/main/protobuf/*.proto
server:
	sbt "project pokerno-server" "run --rpc --websocket --restore src/main/json/restore.json"
build-server:
	sbt "project pokerno-server" assembly
build-server-deps:
	sbt "project pokerno-server" assembly-package-dependency
