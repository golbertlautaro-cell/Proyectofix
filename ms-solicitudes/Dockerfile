# Dockerfile para ms-solicitudes
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR compilado
COPY target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MS_LOGISTICA_URL=http://ms-logistica:8081

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
