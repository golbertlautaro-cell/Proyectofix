# Dockerfile para ms-solicitudes
FROM eclipse-temurin:21-jre-alpine

# Instalar curl para healthcheck
RUN apk add --no-cache curl

WORKDIR /app

# Copiar el JAR compilado
COPY target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MS_LOGISTICA_URL=http://ms-logistica:8081

# Ejecutar la aplicación con opciones de memoria
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
