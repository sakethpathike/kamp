package sakethh.kamp.presentation.blog

import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.ScriptType
import kotlinx.html.onClick
import kotlinx.html.script
import kotlinx.html.unsafe
import sakethh.kamp.blogFileNames
import sakethh.kamp.domain.model.BlogItem
import sakethh.kamp.presentation.common.Header
import sakethh.kamp.presentation.home.divider
import sakethh.kamp.presentation.home.pageMargin
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kapsule.*
import sakethh.kapsule.utils.*

fun BODY.BlogList() {
    Column(
        id = "current_page", modifier = Modifier.pageMargin().fillMaxWidth(0.7)
    ) {
        Header(selectedComponent = "blog")
        Spacer(modifier = Modifier.height(15.px))
        Text(
            text = """
                The following posts are parsed using a custom Markdown parser based on the <a target="_blank" style = "color: ${Colors.primaryDark}" href="https://spec.commonmark.org/0.31.2/#appendix-a-parsing-strategy">CommonMark spec</a>.
            """.trimIndent(),
            fontSize = 14.px,
            fontFamily = Constants.Inter,
            color = Colors.primaryDark,
            fontWeight = FontWeight.Predefined.Thin
        )
        Spacer(modifier = Modifier.height(15.px))
        blogFileNames.forEach {
            BlogItem(BlogItem.getBlogItem(it))
        }
    }
}

private fun FlowContent.BlogItem(blogItem: BlogItem) {
    Column(modifier = Modifier.cursor(Cursor.Pointer).margin(bottom = 20.px), onThisElement = {
        onClick = """
                window.open("/blog/${blogItem.fileName}", "_self");
                """.trimIndent()
    }) {
        Text(
            text = blogItem.blogName,
            color = Colors.primaryDark,
            fontSize = 20.5.px,
            fontWeight = FontWeight.Predefined.Bold,
            fontFamily = Constants.Inter
        )
        Spacer(modifier = Modifier.height(2.5.px))
        if (blogItem.description.isNotBlank()) {
            Text(
                text = blogItem.description,
                fontSize = 16.5.px,
                color = Colors.secondaryDark,
                fontFamily = Constants.Inter
            )
            Spacer(modifier = Modifier.height(3.px))
        } else {
            Spacer(modifier = Modifier.height(1.px))
        }
        Row(horizontalAlignment = HorizontalAlignment.Center, modifier = Modifier.width("fit-content")) {
            Span(
                modifier = Modifier.color(Colors.onSurfaceVariantDark).fontSize(12.px),
                className = "material-symbols-outlined",
                onThisElement = {}) {
                +"calendar_clock"
            }
            Spacer(modifier = Modifier.width(2.5.px))
            Text(
                text = blogItem.pubDateTime,
                color = Colors.onSurfaceVariantDark,
                fontWeight = FontWeight.Predefined.NormalKeyword,
                fontFamily = Constants.Inter,
                fontSize = 14.5.px
            )
        }
    }
}