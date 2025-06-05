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
    implementation("io.ktor:ktor-client-cio-jvm:3.1.3")
}

// because there should be a way to get file names when this server runs via a jar
val generateImageNamesTxt = tasks.register("generateImageNamesTxt") {
    val imageDir = file("src/main/resources/static/images")
    val imageNamesFile = file("src/main/resources/static/images/imagesNames.txt")

    // From https://discuss.gradle.org/t/what-is-dolast-for/27731/2:
    // The doLast creates a task action that runs
    // when the task executes.
    // Without it, you’re running the code at configuration time on every build.
    // Both of these print the line,
    // but the first one only prints the line when the testTask is supposed to be executed.
    // The second one runs it when the build is configured, even if the task should not run.
    doLast {
        imageNamesFile.writeText(imageDir.listFiles()?.filter { it.isFile && it.nameWithoutExtension != "images" }
            ?.joinToString { it.name } ?: "")
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(generateImageNamesTxt)
}

val generateBlogFileNamesTxt = tasks.register("generateBlogFileNamesTxt") {
    val blogDir = file("src/main/resources/blog")
    val blogNamesFile = file("src/main/resources/blog/blogNames.txt")
    doLast {
        blogNamesFile.writeText(blogDir.listFiles()?.filter { it.isFile && it.nameWithoutExtension != "blogNames" }
            ?.joinToString { it.nameWithoutExtension } ?: "")
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(generateBlogFileNamesTxt)
}