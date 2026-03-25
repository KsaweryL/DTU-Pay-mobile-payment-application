FROM eclipse-temurin:21-jre
ADD https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.11/org.jacoco.agent-0.8.11-runtime.jar /opt/jacocoagent.jar
WORKDIR /app
COPY target/token-manager-*.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
