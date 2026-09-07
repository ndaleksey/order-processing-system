FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY payment-service/pom.xml ./payment-service/pom.xml
COPY order-service/pom.xml ./order-service/pom.xml

COPY payment-service/src ./payment-service/src

RUN mvn -pl payment-service -am package -DskipTests


FROM eclipse-temurin:21-jre-noble

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=builder /build/payment-service/target/payment-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

USER app

ENTRYPOINT ["java", "-jar", "app.jar"]