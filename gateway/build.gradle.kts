plugins {
    java
    id("org.springframework.boot")
}

description = "gateway"

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${rootProject.extra["springCloudVersion"]}"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
}