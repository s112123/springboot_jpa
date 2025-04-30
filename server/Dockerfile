FROM openjdk:21-jdk-slim
WORKDIR /server
COPY ./build/libs/server-0.0.1-SNAPSHOT.jar server.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "server.jar"]