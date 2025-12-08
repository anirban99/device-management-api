# --- STAGE 1: Build the JAR file ---
# Use an official JDK 21 image for building (using Maven as per the project template)
FROM maven:3.9.5-eclipse-temurin-21 AS build

# Create a working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies first (leveraging Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the Spring Boot application into an executable JAR
# The -DskipTests flag is used here as tests should ideally run in a separate CI stage.
# If you must run tests inside the container, remove -DskipTests.
RUN mvn clean package -DskipTests

# --- STAGE 2: Create the final, lightweight runtime image ---
# Use a smaller base image (Eclipse Temurin JDK 21 JRE) for the runtime
FROM eclipse-temurin:21-jre-alpine

# Set the argument for the path to the JAR file
ARG JAR_FILE=/app/target/device-management-api-*.jar

# Copy the JAR file from the build stage
COPY --from=build ${JAR_FILE} app.jar

# Define the port the application runs on
EXPOSE 8080

# Define the entry point to run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]