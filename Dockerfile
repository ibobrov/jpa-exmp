# ---------- build stage ----------
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/project

# Кэш зависимостей
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Сборка
COPY . .
RUN ./gradlew --no-daemon clean bootJar

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]