# clinical-data-dictionary

Clinical Attribute Web Service for retrieving clinical attribute names and metadata.

### Requirements

JDK 1.8 or later: http://www.oracle.com/technetwork/java/javase/downloads/index.html

Maven 3.0+: http://maven.apache.org/download.cgi

### Installation and setup

Clone repository, compile, run tests, and start server:
```
$ git clone https://github.com/cBioPortal/clinical-data-dictionary.git

$ cd clinical-data-dictionary

$ export set SERVER_PORT=8091; mvn package -Dpackaging.type=jar && java -jar target/cdd.jar
```


