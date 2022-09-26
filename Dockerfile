FROM openjdk:19-jdk-bullseye

RUN wget https://github.com/processing/processing4/releases/download/processing-1286-4.0.1/processing-4.0.1-linux-x64.tgz
RUN tar xvf processing-4.0.1-linux-x64.tgz
RUN mv processing-4.0.1 /usr/local/bin/processing

RUN export PATH=$PATH:/usr/local/bin/processing

WORKDIR /app

COPY build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
