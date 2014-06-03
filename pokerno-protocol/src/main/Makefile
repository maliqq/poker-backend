ruby:
	for f in $$(ls thrift); do echo $$f; thrift --gen rb -o ./ruby thrift/$$f; done

.PHONY: ruby
