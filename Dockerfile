# Usa Java 21 en una imagen ligera de Alpine
FROM eclipse-temurin:21-jdk-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR compilado al contenedor
COPY target/*.jar app.jar

# Expone el puerto de la app
EXPOSE 8080

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
