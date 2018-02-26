# clinical-attributes

RESTful API to cBioPortal clinical attributes.

### Requirements

JDK 1.8 or later: http://www.oracle.com/technetwork/java/javase/downloads/index.html

Maven 3.0+: http://maven.apache.org/download.cgi

### Installation and setup

Clone repository, compile, run tests, and start server:
```
$ git clone https://github.com/knowledgesystems/clinical-attributes.git

$ cd clinical-attributes

$ export set SERVER_PORT=8091; mvn package -Dpackaging.type=jar && java -jar target/clinical_attributes-0.1.0.jar
```


