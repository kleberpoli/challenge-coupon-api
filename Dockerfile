# Stage 1: Build (Isolated compilation)
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# It executes the packaging, skipping the tests to speed up the image build
RUN mvn clean package -DskipTests

# Stage 2: Runtime (Optimized final image)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# It only copies the artifact generated in the previous stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]