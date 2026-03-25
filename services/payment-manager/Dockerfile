# FROM maven:3.9.9-eclipse-temurin-21 AS build
# WORKDIR /app
# COPY libraries/messaging-utilities /app/libraries/messaging-utilities
# RUN mvn -f /app/libraries/messaging-utilities/pom.xml clean install -DskipTests

# COPY payment-service /app/payment-service
# WORKDIR /app/payment-service
# RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
ADD https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.11/org.jacoco.agent-0.8.11-runtime.jar /opt/jacocoagent.jar
WORKDIR /app
COPY target/payment-manager-*.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
