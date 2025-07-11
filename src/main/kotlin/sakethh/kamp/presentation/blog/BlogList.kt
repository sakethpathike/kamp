package sakethh.kamp.presentation.blog

import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.ScriptType
import kotlinx.html.onClick
import kotlinx.html.script
import kotlinx.html.unsafe
import sakethh.kamp.blogFileNames
import sakethh.kamp.domain.model.BlogItem
import sakethh.kamp.presentation.common.Footer
import sakethh.kamp.presentation.common.Header
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kapsule.*
import sakethh.kapsule.utils.*

fun BODY.BlogList() {
    script(type = ScriptType.textJavaScript) {
        unsafe {
            +"""
      document.addEventListener("DOMContentLoaded", () => {
        const footer = document.getElementById("footer");
        const isMobile = window.matchMedia("(max-width: 767px)").matches;
        
        if(isMobile){
            footer.style.padding = "5px";
            footer.style.width = "99.25%";
        }
      });
      """.trimIndent()
        }
    }
    Column(
        id = "current_page", modifier = Modifier.padding(50.px).fillMaxWidth(0.7)
    ) {
        Header(selectedComponent = "blog")
        Spacer(modifier = Modifier.height(25.px))
        blogFileNames.forEach {
            BlogItem(BlogItem.getBlogItem(it))
        }
    }
    Div(
        id = "footer",
        modifier = Modifier.padding(50.px).position(Position.Fixed).fillMaxWidth(0.7).custom(
            "bottom: 0; left: 0;"
        )
    ) {
        Footer(
            fontSize = 14.px, iconSize = 14.px, enableBorder = false
        )
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