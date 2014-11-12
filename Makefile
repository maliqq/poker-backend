start:
	./sbt "project pokerno-server" "run --enable-auth --redis localhost:6379 --rpc --http-api --websocket --restore ./restore.json --db-props ./etc/database.properties --id 02b82428-fc15-4d66-ba53-bc0b85c1330f"
start-replayer:
	./sbt "project pokerno-replay" "run --http"
start-bots:
	./sbt "project pokerno-ai" "run --db-props ./etc/database.properties --id 30552f31-f871-4d05-aa08-83402a01efba --speed 200ms"
compile:
	./sbt "project pokerno-server" compile
build:
	./sbt "project pokerno-server" assembly
build-deps:
	./sbt "project pokerno-server" assembly-package-dependency
deploy:
	scp $(FILE) root@pokerno.de:/apps/poker-server/bin/server.jar
	make restart
restart:
	ssh root@pokerno.de sv restart poker-server
sloc:
	find . -path ./pokerno-protocol -prune -o -name "*.scala" -print | xargs cloc | grep Scala | awk '{print $$5}'
