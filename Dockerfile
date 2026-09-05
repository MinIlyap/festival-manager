# syntax=docker/dockerfile:1.7
#
# Single Dockerfile shared by all nine Spring Boot services.
#
# The `builder` stage is byte-for-byte identical for every service, so BuildKit
# builds the Gradle project once and the remaining services reuse the cached
# layer. Only the `runtime` stage differs, via the SERVICE build argument.
#
#   docker build --build-arg SERVICE=authservice -t festivalmanager/authservice .

FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace
COPY . .

# The root project has no main class, so bootJar is requested per module
# instead of running the aggregate task.
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
