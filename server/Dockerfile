# 프로젝트를 Gradle 로 빌드
FROM gradle:8.4-jdk21 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon -x test

# 빌드된 JAR 실행
FROM openjdk:21-jdk-slim
WORKDIR /server
COPY --from=builder /app/build/libs/server-0.0.1-SNAPSHOT.jar server.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "server.jar"]