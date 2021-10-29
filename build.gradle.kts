import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val cliVersion: String by project

plugins {
	id("org.springframework.boot") version "2.5.5"
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

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.amazonaws:aws-java-sdk-dynamodb:1.12.80")
	implementation("com.github.derjust:spring-data-dynamodb:5.1.0")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-oauth2-jose")
	implementation("io.github.microutils:kotlin-logging-jvm:2.0.10")
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.90")
	implementation("io.sentry:sentry-spring-boot-starter:5.2.4")
	implementation("io.sentry:sentry-logback:5.2.4")

	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.20")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
	testImplementation("org.testcontainers:testcontainers:1.16.0")
	testImplementation("org.testcontainers:localstack:1.16.0")
	testImplementation("org.testcontainers:junit-jupiter:1.16.0")
	testImplementation("com.tyro.oss:arbitrater:1.0.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
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
