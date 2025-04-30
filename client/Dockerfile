FROM openjdk:21-jdk-slim
WORKDIR /client
COPY ./build/libs/client-0.0.1-SNAPSHOT.jar client.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "client.jar"]