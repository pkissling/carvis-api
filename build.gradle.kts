import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val cliVersion: String by project

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
}

group = "cloud.carvis"
version = if (project.hasProperty("cliVersion")) {
    cliVersion
} else {
    "0.0.1-SNAPSHOT"
}
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}
dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.100"))
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:2.4.1"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.1"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.10")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("com.github.derjust:spring-data-dynamodb:5.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.amazonaws:aws-java-sdk-dynamodb")
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("io.sentry:sentry-spring-boot-starter:5.7.3")
    implementation("io.sentry:sentry-logback:5.7.3")
    implementation("org.imgscalr:imgscalr-lib:4.2")
    implementation("com.auth0:auth0:1.41.0")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("io.awspring.cloud:spring-cloud-starter-aws-messaging")
    implementation("io.awspring.cloud:spring-cloud-starter-aws-ses")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.20")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:testcontainers:1.17.1")
    testImplementation("org.testcontainers:localstack:1.16.0")
    testImplementation("org.testcontainers:junit-jupiter:1.16.0")
    testImplementation("com.tyro.oss:arbitrater:1.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.mock-server:mockserver-junit-jupiter:5.13.2")
    testImplementation("org.awaitility:awaitility:4.1.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
