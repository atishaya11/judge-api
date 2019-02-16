FROM openjdk:8-jdk-alpine

COPY target/judge-api-0.0.1-SNAPSHOT.jar judge-api-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/judge-api-0.0.1-SNAPSHOT.jar"]