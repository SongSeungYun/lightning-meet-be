# 1. 빌드 단계
FROM gradle:8.5.0-jdk17 AS build

WORKDIR /home/gradle/src

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew gradlew.bat ./

RUN ./gradlew build --no-daemon || return 0

COPY src ./src

RUN ./gradlew bootJar --no-daemon -x test


# 2. 실행 단계 (OpenJDK → Eclipse Temurin)
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
