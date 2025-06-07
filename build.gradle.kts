import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

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
        if (imageNamesFile.exists().not()) {
            imageNamesFile.createNewFile()
        }
        imageNamesFile.writeText(imageDir.listFiles()?.filter { it.isFile && it.nameWithoutExtension != "imagesNames" }
            ?.joinToString { it.name } ?: "")
    }
}

val generateBlogFileNamesTxt = tasks.register("generateBlogFileNamesTxt") {
    val blogDir = file("src/main/resources/blog")
    val blogNamesFile = file("src/main/resources/blog/blogNames.txt")
    doLast {
        if (blogNamesFile.exists().not()) {
            blogNamesFile.createNewFile()
        }
        blogNamesFile.writeText(blogDir.listFiles()?.filter { it.isFile && it.nameWithoutExtension != "blogNames" }
            ?.joinToString { it.nameWithoutExtension } ?: "")
    }
}

val generateOGImages by tasks.registering {
    doLast {
        val blogDir = file("src/main/resources/blog")
        blogDir.listFiles()?.filter { it.isFile && it.nameWithoutExtension != "blogNames" }?.forEach { blogFile ->
            val scale = 3
            val imageWidth = 1200 * scale
            val imageHeight = 630 * scale
            val bufferedImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
            val graphics2D = bufferedImage.createGraphics()

            // for background
            graphics2D.color = Color.decode("#131318")

            graphics2D.fillRect(0, 0, imageWidth, imageHeight)

            // kamp logo
            graphics2D.drawImage(
                ImageIO.read(file("src/main/resources/static/images/kamp-og.png")), 225, 300, 355, 364, null
            )

            // for text(s)
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
            )
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

            graphics2D.font = Font("Inter", Font.BOLD, 180)

            // for title
            graphics2D.color = Color.decode("#BFC2FF")

            graphics2D.drawString(
                blogFile?.readText()?.substringAfter("title: ")?.substringBefore("\n")?.trim() ?: "",
                225,
                imageHeight - 600
            )

            // below title
            graphics2D.color = Color.decode("#C5C4DD")
            graphics2D.font = Font(
                "Inter", Font.PLAIN, 75
            )

            graphics2D.drawString(
                "Saketh Pathike", 225, imageHeight - 440
            )

            graphics2D.dispose()

            ImageIO.write(
                bufferedImage,
                "png",
                file("src/main/resources/static/images/ogImage-${blogFile.nameWithoutExtension}.png")
            )
        }
    }
}


tasks.named<ProcessResources>("processResources") {
    dependsOn(generateBlogFileNamesTxt)
    dependsOn(generateOGImages)
    dependsOn(generateImageNamesTxt)
}