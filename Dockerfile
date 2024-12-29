FROM openjdk:17
COPY target/credit-service-0.0.1-SNAPSHOT.jar credit-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/credit-service.jar"]
