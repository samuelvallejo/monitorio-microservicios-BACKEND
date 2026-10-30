FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY services/inventory/pom.xml services/inventory/pom.xml
COPY services/orders/pom.xml services/orders/pom.xml
COPY services/payments/pom.xml services/payments/pom.xml
COPY services/inventory/src services/inventory/src
COPY common/src common/src
RUN mvn -q -pl services/inventory -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/services/inventory/target/inventory-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
