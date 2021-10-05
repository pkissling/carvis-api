import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.31"
	kotlin("plugin.spring") version "1.5.31"
}

group = "cloud.carvis"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}


dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.amazonaws:aws-java-sdk-dynamodb:1.12.80")
	implementation("com.github.derjust:spring-data-dynamodb:5.1.0")

	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.20")
	testImplementation("io.rest-assured:kotlin-extensions:4.4.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
	testImplementation("org.testcontainers:testcontainers:1.16.0")
	testImplementation("org.testcontainers:junit-jupiter:1.16.0")
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
