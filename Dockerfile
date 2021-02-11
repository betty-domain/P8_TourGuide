FROM openjdk:8-jdk-alpine
COPY build/libs/*.jar tourGuide.jar
ENTRYPOINT ["java","-jar","/tourGuide.jar"]