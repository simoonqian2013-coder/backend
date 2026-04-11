FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system spring --ingroup spring

COPY --from=build /build/target/*.jar /app/app.jar
RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring
EXPOSE 8090

ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
