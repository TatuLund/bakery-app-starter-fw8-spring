FROM maven:3.9.12-eclipse-temurin-21 AS build

WORKDIR /app

ARG VAADIN_PRO_KEY
ENV VAADIN_PRO_KEY=${VAADIN_PRO_KEY}

COPY pom.xml ./
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

ARG VAADIN_PRO_KEY
ENV VAADIN_PRO_KEY=${VAADIN_PRO_KEY}

COPY --from=build /app/target/bakery-app-starter-fw8-spring-*.war /app/app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.war"]