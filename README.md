# clinical-attribute-metadata

RESTful API to cBioPortal clinical attribute metadata.

### Requirements

JDK 1.8 or later: http://www.oracle.com/technetwork/java/javase/downloads/index.html

Maven 3.0+: http://maven.apache.org/download.cgi

### Installation and setup

Clone repository, compile, run tests, and start server:
```
$ git clone https://github.com/cBioPortal/clinical-attribute-metadata.git

$ cd clinical-attribute-metadata

$ export set SERVER_PORT=8091; mvn package -Dpackaging.type=jar && java -jar target/clinical-attribute-metadata-0.1.0.jar
```


