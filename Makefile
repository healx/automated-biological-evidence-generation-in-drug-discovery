.PHONY:
build-java:
	mvn -f java/pom.xml package

.PHONY:
lint:
	black python
	isort python
	ruff --fix python

.PHONY:
clean:
	rm -rf java/target
