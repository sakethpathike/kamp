package sakethh.kamp.presentation.home

import kotlinx.coroutines.runBlocking
import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.onMouseDown
import sakethh.kamp.domain.model.RepoDTO
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
            text = "Android Dev ${Typography.bullet} Kotlin ${Typography.bullet} B.Tech CSE student",
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
            text = "Data shown here is pulled from my pinned GitHub repositories.",
            fontWeight = FontWeight.Predefined.Thin,
            fontSize = 14.px,
            color = Colors.primaryDark
        )
        Spacer(modifier = Modifier.height(15.px))
        getPinnedRepos().forEach {
            RepoItem(repoDTO = it)
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
                    This site is built with <a style = "color: ${Colors.primaryDark}" href= "https://github.com/sakethpathike/kapsule" target="_blank">kapsule</a> and served by <a style = "color: ${Colors.primaryDark}" href="https://ktor.io" target="_blank">Ktor</a>.
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

private fun FlowContent.RepoItem(repoDTO: RepoDTO) {
    Column {
        Row(
            horizontalAlignment = HorizontalAlignment.Center,
            modifier = Modifier.cursor(Cursor.Pointer),
            onThisElement = {
                val func =
                    "window.open(\"https://github.com/${if (repoDTO.name.contains("/")) repoDTO.name else "sakethpathike/${repoDTO.name}"}\", \"_blank\");"
                onMouseDown = func
            }) {
            MaterialIcon(iconCode = "open_in_new", modifier = Modifier.color(Colors.primaryDark).fontSize(14.px))
            Spacer(modifier = Modifier.width(2.5.px))
            Text(
                text = repoDTO.name,
                color = Colors.primaryDark,
                fontSize = 18.px,
                fontWeight = FontWeight.Predefined.Bold,
                fontFamily = Constants.Inter
            )
        }
        Spacer(modifier = Modifier.height(5.px))
        Text(
            text = repoDTO.description, fontSize = 16.px, color = Colors.secondaryDark, fontFamily = Constants.Inter
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
                text = repoDTO.starCount, fontFamily = Constants.Inter, color = Colors.secondaryDark,
                fontSize = 14.px
            )
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
fun getPinnedRepos(): List<RepoDTO> = runBlocking {
    val pinnedRepos = mutableListOf<RepoDTO>()
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
                        RepoDTO(
                            name = name,
                            description = description,
                            starCount = starCount,
                            programmingLanguage = programmingLanguage
                        )
                    )
                }
        }
    pinnedRepos.toList()
}