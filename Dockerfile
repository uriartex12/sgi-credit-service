FROM openjdk:17
COPY target/credit_back-0.0.1-SNAPSHOT.jar credit_back.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/credit_back.jar"]
