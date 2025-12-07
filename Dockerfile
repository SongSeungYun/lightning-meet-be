FROM gradle:8.5.0-jdk17 AS build

WORKDIR /home/gradle/src

COPY . .

RUN ./gradlew clean bootJar --no-daemon -x test


FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
