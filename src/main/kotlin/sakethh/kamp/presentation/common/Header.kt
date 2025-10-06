package sakethh.kamp.presentation.common

import kotlinx.html.FlowContent
import kotlinx.html.onClick
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kamp.presentation.utils.blockSelection
import sakethh.kapsule.*
import sakethh.kapsule.utils.Cursor
import sakethh.kapsule.utils.px

fun FlowContent.Header(
    selectedComponent: String, components: List<Triple<String, String, String>> = listOf(
        Triple(first = "Home.kt", second = "home", third = "/"),
        Triple(first = "Blog.kt", second = "blog", third = "/blog")
    )
) {
    val selectedBgColor = Colors.primaryDark
    val selectedTextColor = Colors.onPrimaryDark

    val nonSelectedBgColor = Colors.codeblockBG
    val nonSelectedTextColor = Colors.primaryContainerDark

    Text(
        text = "a",
        fontSize = 125.px,
        fontFamily = Constants.S_CURSIVE_Font,
        color = Colors.primaryDark,
        modifier = Modifier.blockSelection().custom("line-height: 0.8; margin-bottom: -24px; margin-top: -50px; margin-left: -22px")
    )
    Row {
        components.forEach {
            Span(onThisElement = {
                onClick = """
                window.open("${it.third}", "_self");
                """.trimIndent()
            }) {
                InlineCode(
                    code = it.first,
                    modifier = Modifier.backgroundColor(if (selectedComponent == it.second) selectedBgColor else nonSelectedBgColor)
                        .borderRadius(4.px)
                        .color(if (selectedComponent == it.second) selectedTextColor else nonSelectedTextColor)
                        .custom("padding:2px 4px;").fontFamily(Constants.JetBrainsMono).blockSelection()
                        .cursor(Cursor.Pointer)
                )
            }
            Spacer(modifier = Modifier.width(10.px))
        }
    }
}