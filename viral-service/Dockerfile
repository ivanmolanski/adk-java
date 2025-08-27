# Use the official OpenJDK 17 image as the base image
FROM openjdk:17-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the target directory
COPY target/viral-service-0.2.0.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]