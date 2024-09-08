FROM eclipse-temurin:21-jdk-alpine

COPY ./target/test-kafka-cluster-0.0.1-SNAPSHOT.jar /opt/app/application.jar

ENTRYPOINT ["java", "-jar", "/opt/app/application.jar"]

EXPOSE 8080
