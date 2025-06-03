package sakethh.kamp.snapshot

import kotlinx.html.html
import kotlinx.html.stream.createHTML
import sakethh.kamp.KampSurface
import sakethh.kamp.blogFileNames
import sakethh.kamp.presentation.blog.BlogList
import sakethh.kamp.presentation.blog.BlogPage
import sakethh.kamp.presentation.home.Home
import java.lang.IllegalStateException

typealias FileName = String
typealias FileContent = String

object SnapshotManager {
    var isAnySnapshotProcessGoingOn = false

    fun pushANewSnapshot(): Result<String> {
        if (isAnySnapshotProcessGoingOn) return Result.failure(IllegalStateException("A snapshot push is in progress"))

        isAnySnapshotProcessGoingOn = true

        val currentSnapshotName = ""

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

        isAnySnapshotProcessGoingOn = false

        return Result.success("Snapshot $currentSnapshotName has been pushed.")
    }
}