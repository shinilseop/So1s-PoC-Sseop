FROM openjdk:11-jre-slim
COPY build/libs/poc-apiserver-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]