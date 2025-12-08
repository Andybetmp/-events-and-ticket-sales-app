FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/image-service-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
