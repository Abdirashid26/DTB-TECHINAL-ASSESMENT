# ==========================
# Stage 1 - Build with Maven
# ==========================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ==========================
# Stage 2 - Run app
# ==========================
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the JAR from the previous stage
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

