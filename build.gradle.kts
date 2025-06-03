val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "kamp.sakethh"
version = "0.0.1"

application {
    mainClass = "sakethh.kamp.ApplicationKt"
}

repositories {
    mavenCentral()
}

ktor {
    fatJar {
        archiveFileName.set("kamp.jar")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.github.sakethpathike:kapsule:0.1.2")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-auth-jvm")
}
