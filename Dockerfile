FROM openjdk:19-jdk-alpine

WORKDIR /app

COPY build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]