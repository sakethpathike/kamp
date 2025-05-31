package sakethh.kamp.presentation.blog

import kotlinx.html.BODY
import sakethh.kamp.presentation.common.Footer
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kamp.presentation.utils.blockSelection
import sakethh.kapsule.*
import sakethh.kapsule.utils.FontWeight
import sakethh.kapsule.utils.HorizontalAlignment
import sakethh.kapsule.utils.px

fun BODY.Blog(fileName: String) {
    Column(
        id = "current_page", modifier = Modifier.padding(50.px).fillMaxWidth(0.7)
    ) {
        Text(
            text = "Saketh",
            fontWeight = FontWeight.Predefined.SemiBold,
            fontSize = 14.px,
            fontFamily = Constants.Inter,
            color = Colors.secondaryContainerDark
        )
        Spacer(modifier = Modifier.height(10.px))
        Span(onThisElement = {}) {
            InlineCode(
                code = "Blog.kt",
                modifier = Modifier.backgroundColor(Colors.primaryDark).borderRadius(4.px).color(Colors.onPrimaryDark)
                    .custom("padding:2px 4px;").fontFamily(Constants.JetBrainsMono).blockSelection()
            )
        }
        Spacer(modifier = Modifier.height(20.px))
        val blogFile = object {}.javaClass.getResource("/blog/$fileName.md")!!
        val blogMeta = blogFile.readText().substringAfter("---").substringBefore("---").trim()
        val blogTitle = blogMeta.substringAfter("title:").substringBefore("\n").trim()
        val blogDescription = if (blogMeta.substringAfter("---").substringBefore("---").split("\n")[1].trim()
                .startsWith("description")
        ) blogMeta.substringAfter("description:").substringBefore("\n").trim() else ""
        val blogPubDateTime = blogMeta.substringAfter("pubDatetime:").substringBefore("\n").trim()
        Text(
            text = blogTitle,
            color = Colors.primaryDark,
            fontWeight = FontWeight.Predefined.Bold,
            fontFamily = Constants.Inter,
            fontSize = 24.px
        )
        if (blogDescription.isNotBlank()) {
            Spacer(modifier = Modifier.height(5.px))
            Text(
                text = blogDescription,
                color = Colors.secondaryDark,
                fontWeight = FontWeight.Predefined.Medium,
                fontFamily = Constants.Inter,
                fontSize = 16.px
            )
        }
        Spacer(modifier = Modifier.height(5.px))
        Row(horizontalAlignment = HorizontalAlignment.Center, modifier = Modifier.width("fit-content")) {
            Span(
                modifier = Modifier.color(Colors.secondaryDark).fontSize(16.px),
                className = "material-symbols-outlined",
                onThisElement = {}) {
                +"calendar_clock"
            }
            Spacer(modifier = Modifier.width(5.px))
            Text(
                text = blogPubDateTime,
                color = Colors.secondaryDark,
                fontWeight = FontWeight.Predefined.Normal,
                fontFamily = Constants.Inter,
                fontSize = 14.px
            )
        }
        Spacer(modifier = Modifier.height(25.px))
        Text(
            color = Colors.onSurfaceDark,
            fontWeight = FontWeight.Predefined.Normal,
            fontFamily = Constants.Inter,
            fontSize = 18.px,
            text = blogFile.readText().substringAfter("pubDatetime").substringAfter("---").trim()
        )
        Footer()
    }
}