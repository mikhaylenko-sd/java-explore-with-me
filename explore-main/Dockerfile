FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar explore-main.jar
ENTRYPOINT ["java", "-jar", "explore-main.jar"]