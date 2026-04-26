FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY target/*.jar app.jar

ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005","-jar","app.jar"]