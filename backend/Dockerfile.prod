FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Set timezone for logs
ENV TZ=Asia/Ho_Chi_Minh

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
