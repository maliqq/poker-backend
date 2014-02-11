protostuff:
	java -jar ~/Downloads/protostuff-compiler-1.0.7-jarjar.jar project/protostuff.properties
protoc-ruby:
	protoc --ruby_out=src/main/ruby/proto --proto_path=src/main/protobuf/ src/main/protobuf/*.proto
