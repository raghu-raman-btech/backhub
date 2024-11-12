plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation ("com.amazonaws:aws-lambda-java-core:1.2.2")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("commons-io:commons-io:2.6")
    implementation("io.lettuce:lettuce-core:6.1.4.RELEASE")
    implementation("com.amazonaws:aws-java-sdk-apigatewaymanagementapi:1.12.169")
    implementation("net.mguenther.idem:idem-core:0.1.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    val zipTask by register("createZip", Zip::class) {
        from(processResources)
        from(compileKotlin)
        archiveFileName.set("backhubWebsocketLambda.zip")
        into("lib") {
            from(configurations.runtimeClasspath)
        }
    }

    build {
        dependsOn(zipTask)
    }

}