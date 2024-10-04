# Stage 1: Build the application
FROM gradle:7.0.0-jdk11 as builder

# Set the working directory inside the container
WORKDIR /app

# Copy only Gradle wrapper files first (if they exist) to leverage Docker caching
COPY gradlew* gradle/ /app/

# Copy the rest of the project files
COPY . /app

# Run the Gradle build
RUN gradle build --stacktrace --info

# Stage 2: Create the final runtime image
FROM openjdk:11-jre-slim

# Set the working directory for the final image
WORKDIR /app

# Copy the build artifacts from the builder stage to this final image
COPY --from=builder /app/build/libs /app

# Specify the command to run your application (if applicable)
CMD ["java", "-jar", "your-app.jar"]
