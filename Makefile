start:
	./sbt "project pokerno-server" "run -c ./etc/config.json"

compile:
	./sbt "project pokerno-server" compile

build:
	./sbt "project pokerno-server" assembly

build-deps:
	./sbt "project pokerno-server" assembly-package-dependency

deploy: build
	scp ./pokerno-server/target/scala-2.10/pokerno-server-assembly-0.1-SNAPSHOT.jar root@pokerno.de:/apps/poker-server/bin/server.jar
	make restart

restart:
	ssh root@pokerno.de sv restart poker-server
	ssh deploy@staging.pokerno.de sudo sv restart wsearch

sloc:
	find . -path ./pokerno-protocol -prune -o -name "*.scala" -print | xargs cloc | grep Scala | awk '{print $$5}'
