FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN

COPY pom.xml /tmp/

COPY src /tmp/src/

WORKDIR /tmp/

RUN mvn clean compile assembly:single

FROM openjdk:8-jre-alpine

WORKDIR /

COPY --from=MAVEN_TOOL_CHAIN /tmp/target/serverclient-0.0.1-SNAPSHOT-jar-with-dependencies.jar newRelic.jar

RUN rm -rf ./tmp

CMD ["java","-jar","newRelic.jar"]

