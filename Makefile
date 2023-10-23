.PHONY:
build-java:
	mvn -f java/pom.xml package

.PHONY:
clean:
	rm -rf java/target
