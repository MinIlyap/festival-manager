# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace
COPY . .

RUN --mount=type=cache,target=/root/.gradle \
    sh ./gradlew --no-daemon \
        :applicationservice:bootJar \
        :authservice:bootJar \
        :companyservice:bootJar \
        :eventservice:bootJar \
        :festivalservice:bootJar \
        :fileservice:bootJar \
        :locationservice:bootJar \
        :notificationservice:bootJar \
        :userservice:bootJar

FROM eclipse-temurin:21-jre-alpine AS runtime

ARG SERVICE
RUN test -n "$SERVICE" || (echo "SERVICE build argument is required" && false)

RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

COPY --from=builder --chown=spring:spring /workspace/${SERVICE}/build/libs/*.jar app.jar

USER spring
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
