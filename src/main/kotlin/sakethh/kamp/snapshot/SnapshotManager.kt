package sakethh.kamp.snapshot

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import sakethh.kamp.KampSurface
import sakethh.kamp.blogFileNames
import sakethh.kamp.domain.model.DeployTarget
import sakethh.kamp.domain.model.MetaTags
import sakethh.kamp.presentation.blog.BlogList
import sakethh.kamp.presentation.blog.BlogPage
import sakethh.kamp.presentation.home.Home
import sakethh.kamp.presentation.utils.Constants
import java.io.File
import java.nio.file.Files
import kotlin.io.path.*

typealias FileName = String
typealias FileContent = String

object SnapshotManager {
    var isAnySnapshotProcessGoingOn = false

    private fun createAFileIfNotExists(isDir: Boolean, path: String) {
        if (Files.exists(Path(path)).not()) {
            if (isDir) {
                Files.createDirectory(Path(path))
            } else {
                Files.createFile(Path(path))
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    suspend fun pushANewSnapshot(): Result<String> {
        if (isAnySnapshotProcessGoingOn) return Result.failure(IllegalStateException("A snapshot push is in progress"))

        isAnySnapshotProcessGoingOn = true

        val homeScreenContent = createHTML().html {
            KampSurface(
                metaTags = MetaTags.HomePage(deployTarget = DeployTarget.GithubPages)
            ) { Home() }
        }

        val blogListContent = createHTML().html {
            KampSurface(
                metaTags = MetaTags.BlogListPage(deployTarget = DeployTarget.GithubPages)
            ) { BlogList() }
        }

        val allBlogs = mutableListOf<Pair<FileName, FileContent>>()

        blogFileNames.forEach { fileName ->
            val fileContent = createHTML().html {
                KampSurface(
                    metaTags = MetaTags.BlogPage(fileName = fileName, deployTarget = DeployTarget.GithubPages)
                ) { BlogPage(fileName = fileName) }
            }
            allBlogs.add(Pair(first = fileName, second = fileContent))
        }

        // temp dir for the current clone
        val tempKampSnapshotDir = createTempDirectory()
        val gitLogFile = createTempFile()
        val lastCommitHash = HttpClient(CIO).use {
            it.get(urlString = "https://api.github.com/repos/sakethpathike/kamp/commits?sha=master&per_page=1")
                .bodyAsText().substringAfter("\"sha\"").substringAfter("\"").substringBefore("\"").trim()
        }

        try {

            // clone the repo
            ProcessBuilder(
                "git",
                "clone",
                "https://github.com/sakethpathike/sakethpathike.github.io.git",
                tempKampSnapshotDir.pathString
            ).redirectAllResponsesTo(gitLogFile.toFile())?.start()?.waitFor()

            // setup;
            // this doesn't have
            // to be as same as clone's because if new files/folders gets added in this server
            // which doesn't exist in remote snapshot yet,
            // this would help for further operations
            listOf<Pair<Boolean, String>>(
                Pair(false, tempKampSnapshotDir.pathString + "/README.md"),
                Pair(false, tempKampSnapshotDir.pathString + "/blog.html"),
                Pair(false, tempKampSnapshotDir.pathString + "/index.html"),
                Pair(true, tempKampSnapshotDir.pathString + "/blog"),
                Pair(true, tempKampSnapshotDir.pathString + "/images")
            ).forEach {
                createAFileIfNotExists(isDir = it.first, path = it.second)
            }

            ProcessBuilder("git", "checkout", "master").directory(tempKampSnapshotDir.toFile()).start().waitFor()

            tempKampSnapshotDir.listDirectoryEntries().forEach { currentDirectoryEntry ->
                if (currentDirectoryEntry.isRegularFile()) {
                    when (currentDirectoryEntry.fileName.toString()) {
                        "index.html" -> {
                            currentDirectoryEntry.writeText(homeScreenContent)
                        }

                        "blog.html" -> {
                            currentDirectoryEntry.writeText(blogListContent)
                        }

                        "README.md" -> {
                            val updatedREADME = """
                              This repo is entirely auto-generated from [kamp](https://github.com/sakethpathike/kamp)@${lastCommitHash}.

- All HTML files are generated using [kapsule](https://github.com/sakethpathike/kapsule).
- Blog markdown files from the kamp repo are converted to HTML using a custom markdown parser combined with kapsule.
- The generated files on the master branch of this repo reflect exactly what kamp would serve @${lastCommitHash}.
- This repo serves as a static snapshot mirror, pushed automatically by kamp-bot.

> Note: HTML files may include JavaScript similar to the live kamp app, so dynamic UI behavior is expected.""".trimIndent()
                            currentDirectoryEntry.writeText(updatedREADME.trim())
                        }
                    }
                }

                if (currentDirectoryEntry.isDirectory() &&
                    // I don't think this really needs an explanation
                    currentDirectoryEntry.fileName.toString() != ".git"
                ) {
                    val currentDirectoryEntryRef = currentDirectoryEntry

                    // we will delete everything
                    // because we don't know what's been changed and what not
                    // when we are at this step in the process,
                    // we can compare it against the files in temp repo,
                    // but this is _alright_
                    currentDirectoryEntry.deleteRecursively()

                    // now that we deleted all of it,
                    // we need
                    // to create the _new_ directory
                    // that we are currently working in
                    // because:
                    // > If the entry located by this path is a directory,
                    // > this function (deleteRecursively)
                    // recursively deletes its content and the directory itself.
                    createAFileIfNotExists(isDir = true, path = currentDirectoryEntryRef.pathString)


                    when (currentDirectoryEntryRef.fileName.toString()) {
                        "blog" -> {
                            allBlogs.forEach { currentBlog ->
                                val filePath = currentDirectoryEntryRef.pathString + "/" + currentBlog.first + ".html"
                                Files.createFile(Path(path = filePath))
                                File(filePath).writeText(currentBlog.second)
                            }
                        }

                        "images" -> {
                            val imageNames =
                                object {}.javaClass.getResourceAsStream("/static/images/imagesNames.txt")?.use {
                                    it.bufferedReader().use {
                                        it.readText().split(",")
                                    }
                                } ?: emptyList()

                            imageNames.map { it.trim() }.forEach { currentImgName ->
                                val filePath = currentDirectoryEntryRef.pathString + "/" + currentImgName

                                object {}.javaClass.getResourceAsStream("/static/images/$currentImgName")
                                    ?.use { inputStream ->
                                        Files.createFile(Path(path = filePath))
                                        File(filePath).outputStream().use { outputStream ->
                                            inputStream.copyTo(outputStream)
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            ProcessBuilder(
                "git", "add", ".", tempKampSnapshotDir.pathString
            ).directory(tempKampSnapshotDir.toFile()).redirectAllResponsesTo(gitLogFile.toFile())?.start()?.waitFor()


            ProcessBuilder(
                "git",
                "commit",
                "--author",
                "kamp-bot  <${System.getenv(Constants.KAMP_BOT_EMAIL).trim()}>",
                "-m",
                "snapshot: sync with kamp@$lastCommitHash"
            ).directory(tempKampSnapshotDir.toFile()).redirectAllResponsesTo(gitLogFile.toFile())?.apply {
                environment().putAll(
                    mapOf(
                        "GIT_COMMITTER_NAME" to "kamp-bot",
                        "GIT_COMMITTER_EMAIL" to System.getenv(Constants.KAMP_BOT_EMAIL)
                    )
                )
            }?.start()?.waitFor()

            ProcessBuilder(
                "git", "push", System.getenv(Constants.KAMP_BOT_PUSH_URL), "HEAD:master"
            ).directory(tempKampSnapshotDir.toFile()).redirectAllResponsesTo(gitLogFile.toFile())?.start()?.waitFor()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        } finally {
            isAnySnapshotProcessGoingOn = false
            tempKampSnapshotDir.deleteRecursively()
        }


        return Result.success(gitLogFile.readText()).also {
            gitLogFile.deleteIfExists()
        }
    }

    private fun ProcessBuilder.redirectAllResponsesTo(file: File): ProcessBuilder? {
        return this.redirectOutput(ProcessBuilder.Redirect.appendTo(file))
            .redirectError(ProcessBuilder.Redirect.appendTo(file))
    }
}