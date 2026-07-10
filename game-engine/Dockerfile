# syntax=docker/dockerfile:1
# Build stage — JDK + Gradle live here, so the host only needs Docker to run.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
# Cache mount keeps Gradle's downloaded deps + build cache between builds and
# shares them across all four images. sharing=locked serializes access so the
# four images building in parallel don't corrupt the shared Gradle cache.
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    chmod +x gradlew && ./gradlew :game-engine:bootJar --no-daemon --build-cache

# Runtime stage — slim JRE plus the jar built above. curl is for the compose healthcheck.
FROM eclipse-temurin:25-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 spring
WORKDIR /app
COPY --from=build /workspace/game-engine/build/libs/*.jar app.jar
USER spring
ENTRYPOINT ["java", "-jar", "app.jar"]
