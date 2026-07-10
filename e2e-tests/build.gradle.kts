plugins {
    java
    id("org.springframework.boot")
}

description = "e2e-tests"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation(platform("org.testcontainers:testcontainers-bom:${rootProject.extra["testcontainersVersion"]}"))
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(
        ":eureka-server:bootJar",
        ":gateway:bootJar",
        ":game-engine:bootJar",
        ":session-service:bootJar"
    )
}