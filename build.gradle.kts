import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    kotlin("jvm") version "_"
    `java-test-fixtures`
    id("com.avast.gradle.docker-compose") version "0.17.7"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

repositories {
    mavenCentral()
}

apply(plugin = "kotlin")
apply(plugin = "idea")


dependencies {
    implementation(platform("dev.forkhandles:forkhandles-bom:_"))
    implementation("dev.forkhandles:result4k")

    implementation(platform("org.jetbrains.exposed:exposed-bom:_"))
    implementation("org.jetbrains.exposed:exposed-jdbc")
    implementation("org.jetbrains.exposed:exposed-json")
    implementation("org.jetbrains.exposed:exposed-java-time")

    implementation(platform("com.fasterxml.jackson:jackson-bom:_"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.postgresql:postgresql:_")

    testImplementation(Kotlin.test)
    testImplementation(Testing.strikt.core)
    testImplementation("dev.forkhandles:result4k-strikt")
}

tasks {
    test {
        testLogging {
            events = setOf(FAILED, PASSED, SKIPPED, STANDARD_OUT)
            exceptionFormat = FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}

dockerCompose {
    isRequiredBy(tasks.test)

    captureContainersOutput = false
    stopContainers = true

    // Workaround for MAC - something is clunky when looking for docker command
    if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        executable = "/usr/local/bin/docker-compose"
        dockerExecutable = "/usr/local/bin/docker"
    }
}
