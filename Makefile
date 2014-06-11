start:
	sbt "project pokerno-server" "run --rpc --http-api --websocket --restore ./restore.json"
start-replayer:
	sbt "project pokerno-replay" "run --http"
compile:
	sbt "project pokerno-server" compile
build:
	sbt "project pokerno-server" assembly
build-deps:
	sbt "project pokerno-server" assembly-package-dependency
deploy:
	scp $(FILE) root@pokerno:/apps/poker-server/bin/server.jar
sloc:
	find . -path ./pokerno-protocol/src/main/scala/de/pokerno/protocol/thrift -prune -o -name "*.scala" -print | xargs cloc | grep Scala | awk '{print $$5}'
