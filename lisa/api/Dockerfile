FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home --shell /usr/sbin/nologin lisa
WORKDIR /app
COPY --from=build /workspace/target/lisa-api-0.1.0.jar app.jar
USER lisa
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]