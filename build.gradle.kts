plugins {
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("jvm") version "1.9.24" // Update to the latest stable Kotlin version
	kotlin("plugin.spring") version "1.9.24"
}

group = "com.msp"
version = "0.0.1-SNAPSHOT"

apply(plugin = "java")
apply(plugin = "kotlin")


java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
//	sourceCompatibility = JavaVersion.VERSION_17
//	targetCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-quartz")
	implementation("org.springframework.security:spring-security-config")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("software.amazon.awssdk:s3:2.20.55")
	implementation("mysql:mysql-connector-java:8.0.28")
	implementation("io.jsonwebtoken:jjwt:0.9.1")
	implementation("com.google.code.gson:gson:2.11.0")
	implementation("com.mchange:c3p0:0.9.5.5")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly ("com.h2database:h2")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation ("com.amazonaws:aws-java-sdk-s3:1.12.528")
	implementation("org.projectlombok:lombok:1.18.24") // Lombok dependency
	annotationProcessor("org.projectlombok:lombok:1.18.24")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}


tasks.withType<Test> {
	useJUnitPlatform()
}
