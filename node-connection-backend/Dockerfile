FROM gradle:8.8-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./

RUN gradle build --no-daemon --parallel --stacktrace || return 0

COPY src ./src

RUN gradle bootJar --no-daemon --parallel --stacktrace

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 8083