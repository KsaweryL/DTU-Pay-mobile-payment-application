FROM eclipse-temurin:21 as jre-build
ADD https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.11/org.jacoco.agent-0.8.11-runtime.jar /opt/jacocoagent.jar
ADD https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.11/org.jacoco.cli-0.8.11-nodeps.jar /opt/jacococli.jar
WORKDIR /usr/src
COPY target/quarkus-app /usr/src/quarkus-app
CMD java -Xmx64m \
    -javaagent:/opt/jacocoagent.jar=output=tcpserver,address=0.0.0.0,port=6303,inclnolocationclasses=true \
    -jar quarkus-app/quarkus-run.jar 
