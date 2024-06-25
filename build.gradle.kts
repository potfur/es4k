import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
     kotlin("jvm") version "2.0.0"
    `java-test-fixtures`
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

    testApi(Kotlin.test)
    testApi(Testing.strikt.core)
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
