package sakethh.kamp.presentation.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.html.BODY
import kotlinx.html.FlowContent
import org.jsoup.Jsoup
import sakethh.kamp.domain.model.RepoDTO
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kapsule.*
import sakethh.kapsule.utils.FontWeight
import sakethh.kapsule.utils.HorizontalAlignment
import sakethh.kapsule.utils.Shape
import sakethh.kapsule.utils.px

fun BODY.Home() {
    Column(
        modifier = Modifier.padding(15.px)
    ) {
        Span(onThisElement = {}) {
            InlineCode(
                code = "Home.kt",
                modifier = Modifier.backgroundColor("#BFC2FF").borderRadius(4.px).color("#272B60")
                    .custom("padding:2px 4px;").fontFamily(Constants.JetBrainsMono)
            )
        }
        Spacer(modifier = Modifier.height(25.px))
        Text(
            text = "Saketh Pathike",
            fontWeight = FontWeight.Predefined.Bolder,
            fontSize = 24.px,
            fontFamily = Constants.Inter,
            color = Colors.primaryDark
        )
        Text(
            text = "Android Dev ${Typography.bullet} Kotlin ${Typography.bullet} B.Tech CSE student",
            fontWeight = FontWeight.Predefined.Medium,
            fontSize = 18.px,
            fontFamily = Constants.Inter,
            color = Colors.onSurfaceDark
        )
        Spacer(modifier = Modifier.height(5.px))
        Text(
            fontFamily = Constants.Inter,
            text = "i build stuff <i>passionately</i>.",
            fontWeight = FontWeight.Predefined.Normal,
            fontSize = 16.px,
            color = Colors.onSurfaceDark
        )
        Spacer(modifier = Modifier.height(15.px))
        Row {
            ContactItem(imageSrc = "/images/github.svg", string = "Github")
            Spacer(modifier = Modifier.width(5.px))
            ContactItem(imageSrc = "mail_outline", string = "Email")
            Spacer(modifier = Modifier.width(5.px))
            ContactItem(imageSrc = "/images/twitter.svg", string = "Twitter/X")
            Spacer(modifier = Modifier.width(5.px))
            ContactItem(imageSrc = "/images/linkedin.svg", string = "LinkedIn")
        }
        Spacer(modifier = Modifier.height(25.px))
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
            RepoItem(it)
            Spacer(modifier = Modifier.height(15.px))
        }
    }
    Spacer(modifier = Modifier.height(250.px))
}

private fun FlowContent.RepoItem(repoDTO: RepoDTO) {
    Column {
        Text(
            text = repoDTO.name, color = Colors.primaryDark, fontSize = 18.px, fontWeight = FontWeight.Predefined.Bold, fontFamily = Constants.Inter
        )
        Spacer(modifier = Modifier.height(5.px))
        Text(text = repoDTO.description, fontSize = 16.px,color = Colors.onSurfaceDark, fontFamily = Constants.Inter)
        Spacer(modifier = Modifier.height(5.px))
        Row(
            horizontalAlignment = HorizontalAlignment.Center,
        ) {
            MaterialIcon(iconCode = "star_outline", modifier = Modifier.fontSize(18.px).color(Colors.onSurfaceDark))
            Spacer(modifier = Modifier.width(5.px))
            Text(
                text = repoDTO.starCount,
                fontFamily = Constants.Inter,
                color = Colors.onSurfaceDark,
                fontSize = 14.px
            )
        }
    }
}

private fun FlowContent.ContactItem(imageSrc: String, string: String) {
    Row(
        horizontalAlignment = HorizontalAlignment.Center,
        modifier = Modifier.backgroundColor(Colors.ButtonContainerColor).clip(Shape.RoundedRectangle(1.5.px))
            .color(Colors.ButtonContentColor).padding(5.px).width("fit-content")
    ) {
        if (imageSrc.startsWith("/")) {
            Image(src = imageSrc, modifier = Modifier.size(18.px))
        } else {
            MaterialIcon(iconCode = imageSrc, modifier = Modifier.fontSize(18.px))
        }
        Spacer(modifier = Modifier.width(5.px))
        Text(
            text = string,
            fontWeight = FontWeight.Predefined.Medium,
            fontFamily = Constants.Inter,
        )
    }
}

// as of now this is directly used in the presentation layer, ig that's not how it works (according to clean arch 🤓☝️)
fun getPinnedRepos(): List<RepoDTO> = runBlocking {
    val pinnedRepos = mutableListOf<RepoDTO>()
    withContext(Dispatchers.IO) {
        Jsoup.connect("https://github.com/sakethpathike").get().toString().let {
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
    }
    pinnedRepos.toList()
}