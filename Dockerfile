# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Use a non-root user for better security in production
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

# Render will override this, but it's good practice
EXPOSE 10000

# Tell Spring to use the PORT variable provided by Render
ENTRYPOINT ["java", "-Dserver.port=${PORT:1212}", "-jar", "app.jar"]
