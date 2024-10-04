# Stage 1: Build the application with Gradle and Java 17
FROM gradle:8.0.0-jdk17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew gradlew.bat gradle/wrapper/ /app/

# Ensure the Gradle wrapper script is executable
RUN chmod +x /app/gradlew

# Copy the rest of the project files
COPY . /app

# Run the Gradle build using bash
RUN /bin/bash -c "./gradlew build --stacktrace --info"

# Stage 2: Create the final runtime image using Java 17
FROM openjdk:17-slim

# Set the working directory for the final image
WORKDIR /app

# Copy the build artifacts from the builder stage to this final image
COPY --from=builder /app/build/libs /app

# Specify the command to run your application (replace 'your-app.jar' with the actual JAR name)
CMD ["java", "-jar", "Lab2Template-1.0-SNAPSHOT.jar"]
