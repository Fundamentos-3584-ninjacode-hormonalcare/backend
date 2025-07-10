# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Etapa 2: Run
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
