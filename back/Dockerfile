
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src
COPY docs ./docs

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/target/nautiflow-0.0.1-SNAPSHOT.jar /app/nautiflow.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/nautiflow.jar"]
