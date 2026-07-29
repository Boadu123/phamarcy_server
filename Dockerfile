FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x ./mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SERVER_PORT=8080
ENV JAVA_OPTS=""

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
