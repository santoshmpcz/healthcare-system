FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/HealthCare-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9898

ENTRYPOINT ["java","-jar","app.jar"]