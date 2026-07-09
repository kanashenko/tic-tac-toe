plugins {
    java
    id("org.springframework.boot")
}

description = "game-engine"

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${rootProject.extra["springCloudVersion"]}"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
}