FROM eclipse-temurin:21 AS jre-build
ADD https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.11/org.jacoco.agent-0.8.11-runtime.jar /opt/jacocoagent.jar
COPY target/account-manager-1.0.0-SNAPSHOT.jar /usr/src/
WORKDIR /usr/src/
CMD ["java", "-jar", "account-manager-1.0.0-SNAPSHOT.jar"]

