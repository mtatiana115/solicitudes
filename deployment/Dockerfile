# Etapa de build con Gradle + JDK 18
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test -x validateStructure --no-daemon

# Etapa de runtime con JDK 17
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/applications/app-service/build/libs/Solicitudes.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
