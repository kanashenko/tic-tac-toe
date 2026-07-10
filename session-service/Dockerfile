# syntax=docker/dockerfile:1
# Build stage — JDK + Gradle live here, so the host only needs Docker to run.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
# Cache mount keeps Gradle's downloaded deps + build cache between builds and
# shares them across all four images. sharing=locked serializes access so the
# four images building in parallel don't corrupt the shared Gradle cache.
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    chmod +x gradlew && ./gradlew :session-service:bootJar --no-daemon --build-cache

# Runtime stage — slim JRE plus the jar built above.
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 1001 spring
WORKDIR /app
COPY --from=build /workspace/session-service/build/libs/*.jar app.jar
USER spring
ENTRYPOINT ["java", "-jar", "app.jar"]
