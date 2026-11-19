FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace/backend

COPY backend/pom.xml pom.xml
RUN mvn -B dependency:go-offline

COPY backend/src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring --create-home spring
COPY --from=build --chown=spring:spring /workspace/backend/target/smartspend-backend-1.0.0.jar /app/app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.port=${PORT:-8080}"]
