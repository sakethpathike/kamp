package sakethh.kamp.presentation.home

import kotlinx.coroutines.runBlocking
import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.onMouseDown
import sakethh.kamp.domain.model.GithubRepoDTO
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kamp.presentation.utils.blockSelection
import sakethh.kapsule.*
import sakethh.kapsule.utils.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun BODY.Home() {
    Column(
        id = "home_page", modifier = Modifier.padding(50.px).fillMaxWidth(0.7)
    ) {
        Span(onThisElement = {}) {
            InlineCode(
                code = "Home.kt",
                modifier = Modifier.backgroundColor(Colors.primaryDark).borderRadius(4.px).color(Colors.onPrimaryDark)
                    .custom("padding:2px 4px;").fontFamily(Constants.JetBrainsMono).blockSelection()
            )
        }
        Spacer(modifier = Modifier.height(25.px))
        Text(
            text = "Saketh Pathike", fontWeight = FontWeight.Predefined.Black, fontSize = 32.px,
            fontFamily = Constants.Inter,
            color = Colors.primaryDark
        )
        Text(
            text = "Android Dev • Kotlin Multiplatform • B.Tech CSE student",
            fontWeight = FontWeight.Predefined.Medium,
            fontSize = 18.px,
            fontFamily = Constants.Inter,
            color = Colors.secondaryDark
        )
        Text(
            fontFamily = Constants.Inter,
            text = "i build stuff <i>passionately</i>.",
            fontWeight = FontWeight.Predefined.Normal,
            fontSize = 16.px,
            color = Colors.secondaryDark
        )
        Spacer(modifier = Modifier.height(20.px))
        Text(
            text = "Profiles\n& Contact",
            fontWeight = FontWeight.Predefined.SemiBold,
            color = Colors.primaryDark,
            fontSize = 24.px,
            fontFamily = Constants.Inter
        )
        Spacer(modifier = Modifier.height(10.px))
        Row(
            modifier = Modifier.display(Display.Flex).custom(
                """
            flex-wrap: wrap; gap: 10px;
        """.trimIndent()
            )
        ) {
            ContactItem(imageSrc = "/images/github.svg", string = "Github", url = "https://github.com/sakethpathike")
            ContactItem(imageSrc = "mail_outline", string = "Email", url = "mailto:sakethpathike@gmail.com")
            ContactItem(imageSrc = "/images/twitter.svg", string = "Twitter/X", url = "https://x.com/sakethpathike")
            ContactItem(
                imageSrc = "/images/linkedin.svg",
                string = "LinkedIn",
                url = "https://www.linkedin.com/in/sakethpathike/"
            )
        }
        Spacer(modifier = Modifier.height(20.px))
        Text(
            text = "Projects",
            fontWeight = FontWeight.Predefined.SemiBold,
            color = Colors.primaryDark,
            fontSize = 24.px,
            fontFamily = Constants.Inter
        )
        Spacer(modifier = Modifier.height(5.px))
        Text(
            fontFamily = Constants.Inter,
            text = "Data includes information from my pinned GitHub repositories and project tags.",
            fontWeight = FontWeight.Predefined.Light,
            fontSize = 14.px,
            color = Colors.primaryDark
        )
        Spacer(modifier = Modifier.height(15.px))
        getPinnedRepos().forEach {
            RepoItem(githubRepoDTO = it)
            Spacer(modifier = Modifier.height(15.px))
        }
        Spacer(modifier = Modifier.height(25.px))
        Column(
            modifier = Modifier.padding(10.px).border(width = 1.5.px, radius = 15.px, color = Colors.primaryDark),
        ) {
            MaterialIcon(iconCode = "info", modifier = Modifier.color(Colors.primaryDark))
            Spacer(modifier = Modifier.height(5.px))
            Text(
                text = """
                    <a style = "color: ${Colors.primaryDark}" href="https://github.com/sakethpathike/kamp">This site</a> is built with <a style = "color: ${Colors.primaryDark}" href= "https://github.com/sakethpathike/kapsule" target="_blank">kapsule</a> and served by <a style = "color: ${Colors.primaryDark}" href="https://ktor.io" target="_blank">Ktor</a>.
                """.trimIndent(),
                fontSize = 16.px,
                fontFamily = Constants.Inter,
                color = Colors.primaryDark,
                fontWeight = FontWeight.Predefined.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(25.px))
    }
}


private fun FlowContent.RepoItem(githubRepoDTO: GithubRepoDTO) {
    Column {
        Row(
            horizontalAlignment = HorizontalAlignment.Center,
            modifier = Modifier.cursor(Cursor.Pointer),
            onThisElement = {
                val func =
                    "window.open(\"https://github.com/${if (githubRepoDTO.name.contains("/")) githubRepoDTO.name else "sakethpathike/${githubRepoDTO.name}"}\", \"_blank\");"
                onMouseDown = func
            }) {
            MaterialIcon(iconCode = "open_in_new", modifier = Modifier.color(Colors.primaryDark).fontSize(14.px))
            Spacer(modifier = Modifier.width(2.5.px))
            Text(
                text = githubRepoDTO.name,
                color = Colors.primaryDark,
                fontSize = 18.px,
                fontWeight = FontWeight.Predefined.Bold,
                fontFamily = Constants.Inter
            )
        }
        Spacer(modifier = Modifier.height(5.px))
        Text(
            text = githubRepoDTO.description,
            fontSize = 16.px,
            color = Colors.secondaryDark,
            fontFamily = Constants.Inter
        )
        Spacer(modifier = Modifier.height(5.px))
        Row(
            horizontalAlignment = HorizontalAlignment.Center,
        ) {
            MaterialIcon(
                iconCode = "star_outline",
                modifier = Modifier.fontSize(18.px).color(Colors.secondaryDark).blockSelection()
            )
            Spacer(modifier = Modifier.width(5.px))
            Text(
                text = githubRepoDTO.starCount, fontFamily = Constants.Inter, color = Colors.secondaryDark,
                fontSize = 14.px
            )
        }
        Spacer(modifier = Modifier.height(5.px))
        Row(
            modifier = Modifier.display(Display.Flex).custom(
                """
            flex-wrap: wrap; gap: 5px;
        """.trimIndent()
            )
        ) {
            githubRepoDTO.tags.forEach {
                Text(
                    text = it,
                    color = Colors.secondaryContainerDark,
                    fontWeight = FontWeight.Predefined.Normal,
                    fontFamily = Constants.Inter,
                    fontSize = 12.5.px,
                    modifier = Modifier.padding(2.5.px)
                )
            }
        }
    }
}

private fun FlowContent.ContactItem(imageSrc: String, string: String, url: String) {
    Row(
        horizontalAlignment = HorizontalAlignment.Center,
        modifier = Modifier.blockSelection().cursor(Cursor.Pointer).backgroundColor(Colors.primaryContainerDark)
            .clip(Shape.RoundedRectangle(1.5.px)).color(Colors.onPrimaryContainerDark).padding(6.9.px).width("fit-content"),
        onThisElement = {
            val func = "window.open(\"${url}\", \"_blank\");"
            onMouseDown = func
        }
    ) {
        if (imageSrc.startsWith("/")) {
            Image(src = imageSrc, modifier = Modifier.size(20.px))
        } else {
            MaterialIcon(iconCode = imageSrc, modifier = Modifier.fontSize(20.px))
        }
        Spacer(modifier = Modifier.width(5.px))
        Text(
            text = string, fontWeight = FontWeight.Predefined.Medium, fontFamily = Constants.Inter, fontSize = 16.px
        )
    }
}

// as of now this is directly used in the presentation layer, ig that's not how it works (according to clean arch 🤓☝️)
fun getPinnedRepos(): List<GithubRepoDTO> = runBlocking {
    val pinnedRepos = mutableListOf<GithubRepoDTO>()
    HttpClient.newHttpClient().send(
        HttpRequest.newBuilder().GET().uri(URI.create("https://github.com/sakethpathike")).build(),
        HttpResponse.BodyHandlers.ofString()
    ).body().toString().let {
            it.substringAfter("<div class=\"js-pinned-items-reorder-container\">").substringAfter("<ol")
                .substringBefore("</ol>").split("<li").drop(1).forEach {
                    val pinnedItem =
                        it.substringAfter("<div class=\"pinned-item-list-item-content\">").substringAfter("</a>")

                    val name = pinnedItem.substringAfter("<div class=\"d-flex width-full position-relative\">")
                        .substringBefore("<p class=\"pinned-item-desc").substringAfter("<tool-tip")
                        .substringAfter("\">").substringBefore("</tool-tip>").trim()

                    val description = pinnedItem.substringAfter("<p class=\"pinned-item-desc").substringAfter("\">")
                        .substringBefore("</p>").trim()

                    val starCount = pinnedItem.substringAfter("<svg aria-label=\"stars\"").substringAfter("</svg>")
                        .substringBefore("</a>").trim()

                    val programmingLanguage =
                        pinnedItem.substringAfter("<span itemprop=\"programmingLanguage\">").substringBefore("</span>")
                            .trim()
                    pinnedRepos.add(
                        GithubRepoDTO(
                            name = name,
                            description = description,
                            starCount = starCount,
                            programmingLanguage = programmingLanguage,
                            tags = tags.find {
                                it.repoName == name
                            }?.tags ?: emptyList()
                        )
                    )
                }
        }
    pinnedRepos.toList()
}

data class Tags(val repoName: String, val tags: List<String>)

private val tags = listOf(
    Tags(
        repoName = "LinkoraApp/Linkora", tags = listOf(
            Constants.Kotlin,
            Constants.KMP,
            Constants.CMP,
            Constants.ANDROID_SDK,
            Constants.ANDROID_JETPACK,
            Constants.KtorClient,
            Constants.RealTimeSync
        )
    ), Tags(
        repoName = "LinkoraApp/sync-server", tags = listOf(
            Constants.Kotlin, Constants.KtorServer, Constants.Exposed, Constants.RealTimeSync
        )
    ), Tags(
        repoName = "kapsule", tags = listOf(
            Constants.Kotlin, Constants.KMP, Constants.kotlinxHtml, Constants.KT_DSL
        )
    ), Tags(
        repoName = "JetSpacer", tags = listOf(
            Constants.Kotlin, Constants.ANDROID_SDK, Constants.JetpackCompose, Constants.KtorClient
        )
    )
)