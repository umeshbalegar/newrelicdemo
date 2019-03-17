FROM maven:3.5-jdk-8 as maven

# copy the project files
COPY ./pom.xml ./pom.xml

# build all dependencies
RUN mvn dependency:go-offline -B

# copy your other files
COPY ./src ./src

# build for release
RUN mvn clean compile assembly:single

# our final base image
FROM openjdk:8u171-jre-alpine

# set deployment directory
WORKDIR /my-project

# copy over the built artifact from the maven image
COPY --from=maven target/serverclient-*.jar ./serverclient.jar

# set the startup command to run your binary

ENTRYPOINT ["java","-jar","serverclient.jar"]

CMD [""]

