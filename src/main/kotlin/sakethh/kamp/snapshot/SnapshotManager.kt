package sakethh.kamp.snapshot

import kotlinx.html.html
import kotlinx.html.stream.createHTML
import sakethh.kamp.KampSurface
import sakethh.kamp.blogFileNames
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
    fun pushANewSnapshot(): Result<String> {
        if (isAnySnapshotProcessGoingOn) return Result.failure(IllegalStateException("A snapshot push is in progress"))

        isAnySnapshotProcessGoingOn = true

        val homeScreenContent = createHTML().html {
            KampSurface { Home() }
        }

        val blogListContent = createHTML().html {
            KampSurface { BlogList() }
        }

        val allBlogs = mutableListOf<Pair<FileName, FileContent>>()

        blogFileNames.forEach { fileName ->
            val fileContent = createHTML().html { KampSurface { BlogPage(fileName = fileName) } }
            allBlogs.add(Pair(first = fileName, second = fileContent))
        }

        // temp dir for the current clone
        val tempDir = createTempDirectory()
        val lastCommitLogFile = createTempFile()
        val gitLogFile = createTempFile()
        var lastCommitHash: String

        try {

            // clone the repo
            ProcessBuilder(
                "git", "clone", "https://github.com/sakethpathike/sakethpathike.github.io.git", tempDir.pathString
            ).redirectAllResponsesTo(gitLogFile.toFile())?.start()?.waitFor()

            // setup
            listOf<Pair<Boolean, String>>(
                Pair(false, tempDir.pathString + "/README.md"),
                Pair(false, tempDir.pathString + "/blog.html"),
                Pair(false, tempDir.pathString + "/index.html"),
                Pair(true, tempDir.pathString + "/blog")
            ).forEach {
                createAFileIfNotExists(isDir = it.first, path = it.second)
            }

            // get the last commit hash
            ProcessBuilder("git", "rev-parse", "HEAD").directory(tempDir.toFile())
                .redirectAllResponsesTo(lastCommitLogFile.toFile())?.start()?.waitFor()

            ProcessBuilder("git", "checkout", "master").directory(tempDir.toFile()).start().waitFor()

            tempDir.listDirectoryEntries().forEach { workingGitDirFile ->
                if (workingGitDirFile.isRegularFile()) {
                    when (workingGitDirFile.fileName.toString()) {
                        "index.html" -> {
                            workingGitDirFile.writeText(homeScreenContent)
                        }

                        "blog.html" -> {
                            workingGitDirFile.writeText(blogListContent)
                        }

                        "README.md" -> {
                            var updatedREADME = workingGitDirFile.readText().split("\n").dropLast(1).joinToString("\n")
                            updatedREADME += "\n\nThis snapshot was generated from kamp commit ${
                                lastCommitLogFile.readText().trim()
                            }"
                            workingGitDirFile.writeText(updatedREADME.trim())
                        }
                    }
                }

                if (workingGitDirFile.isDirectory()) {
                    val currDirPath = workingGitDirFile.pathString
                    when (workingGitDirFile.fileName.toString()) {
                        "blog" -> {

                            // we will delete everything
                            // because we don't know what's been changed and what not
                            // when we are at this step in the process,
                            // we can compare it against the files in temp repo,
                            // but this is _alright_

                            workingGitDirFile.deleteRecursively()

                            // now that we deleted all of it,
                            // we need
                            // to create the _new_ directory
                            // that we are currently working in
                            // because:
                            // > If the entry located by this path is a directory,
                            // > this function (deleteRecursively)
                            // recursively deletes its content and the directory itself.
                            createAFileIfNotExists(isDir = true, path = currDirPath)

                            allBlogs.forEach { currentBlog ->
                                val filePath = workingGitDirFile.pathString + "/" + currentBlog.first + ".html"
                                Files.createFile(Path(path = filePath))
                                File(filePath).writeText(currentBlog.second)
                            }
                        }
                    }
                }
            }

            ProcessBuilder(
                "git", "add", ".", tempDir.pathString
            ).directory(tempDir.toFile()).redirectAllResponsesTo(gitLogFile.toFile())?.start()?.waitFor()

            lastCommitHash = lastCommitLogFile.readText()

            ProcessBuilder(
                "git",
                "commit",
                "--author",
                "kamp-bot  <${System.getenv(Constants.KAMP_BOT_EMAIL).trim()}>",
                "-m",
                "snapshot: sync with kamp@$lastCommitHash"
            ).directory(tempDir.toFile()).redirectAllResponsesTo(gitLogFile.toFile())?.apply {
                environment().putAll(
                    mapOf(
                        "GIT_COMMITTER_NAME" to "kamp-bot",
                        "GIT_COMMITTER_EMAIL" to System.getenv(Constants.KAMP_BOT_EMAIL)
                    )
                )
            }?.start()?.waitFor()

            ProcessBuilder(
                "git", "push", "-f", System.getenv(Constants.KAMP_BOT_PUSH_URL), "HEAD:master"
            ).directory(tempDir.toFile()).redirectAllResponsesTo(gitLogFile.toFile())?.start()?.waitFor()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        } finally {
            isAnySnapshotProcessGoingOn = false
            tempDir.deleteRecursively()
            lastCommitLogFile.deleteIfExists()
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