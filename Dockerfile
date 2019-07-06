FROM maven:3.5.3-jdk-8 as mvn
COPY . .
RUN mvn package

FROM openjdk:8-jre-slim
RUN apt update && apt install -y jq
COPY /assets/ /opt/resource/
COPY --from=mvn target/concourse-overops.jar /artifact/concourse-overops.jar