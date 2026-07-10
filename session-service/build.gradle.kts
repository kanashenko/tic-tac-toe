plugins {
    java
    id("org.springframework.boot")
}

description = "session-service"

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${rootProject.extra["springCloudVersion"]}"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.mapstruct:mapstruct:${rootProject.extra["mapstructVersion"]}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${rootProject.extra["mapstructVersion"]}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${rootProject.extra["lombokMapstructBindingVersion"]}")

    runtimeOnly("com.h2database:h2")

    testImplementation("io.projectreactor:reactor-test")
}