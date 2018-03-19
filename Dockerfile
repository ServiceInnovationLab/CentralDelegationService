FROM openjdk:8-jdk-alpine
COPY target/cds-swarm.jar /opt/

EXPOSE 8080 9990
ENTRYPOINT ["java", "-jar", "/opt/cds-swarm.jar"]

