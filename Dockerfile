# Збірка з кореня репозиторію: nimmDa_backend.
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY nimmda-domain/pom.xml nimmda-domain/
COPY nimmda-application/pom.xml nimmda-application/
COPY nimmda-infrastructure/pom.xml nimmda-infrastructure/
COPY nimmda-web/pom.xml nimmda-web/

RUN mvn dependency:go-offline -B -pl nimmda-web -am

COPY nimmda-domain/src nimmda-domain/src
COPY nimmda-application/src nimmda-application/src
COPY nimmda-infrastructure/src nimmda-infrastructure/src
COPY nimmda-web/src nimmda-web/src

RUN mvn -pl nimmda-web -am package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/nimmda-web/target/app.jar app.jar
RUN chown -R app:app /app

USER app

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --server.port=${PORT:-8080}"]
