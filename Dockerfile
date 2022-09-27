FROM gradle:7.5.1-jdk18-alpine as builder
WORKDIR /app

COPY build.gradle settings.gradle /app/
RUN gradle clean build > /dev/null 2>&1 || true

COPY . .

RUN gradle clean build --stacktrace --debug --no-daemon

FROM openjdk:18-jdk-bullseye

RUN wget https://github.com/processing/processing4/releases/download/processing-1286-4.0.1/processing-4.0.1-linux-x64.tgz
RUN tar xvf processing-4.0.1-linux-x64.tgz
RUN mv processing-4.0.1 /usr/local/bin/processing

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java","-XX:MaxMetaspaceSize=128m","-Xmx512m","-jar","app.jar"]
