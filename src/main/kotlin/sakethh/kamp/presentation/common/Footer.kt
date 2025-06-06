package sakethh.kamp.presentation.common

import kotlinx.html.FlowContent
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kapsule.*
import sakethh.kapsule.utils.FontWeight
import sakethh.kapsule.utils.px

fun FlowContent.Footer(
    text: String = """
            This site is built with <a style="color: ${Colors.primaryDark}" href="https://github.com/sakethpathike/kapsule" target="_blank">kapsule</a> and powered by <a style="color: ${Colors.primaryDark}" href="https://github.com/sakethpathike/kamp">kamp</a>.
                """.trimIndent(),
    fontSize: String = 16.px,
    fontWeight: FontWeight = FontWeight.Predefined.SemiBold,
    borderWidth: String = 1.5.px,
    iconSize: String? = null,
    enableBorder: Boolean = true,
    padding: String = 10.px
) {
    Spacer(modifier = Modifier.height(25.px))
    Column(
        modifier = Modifier.padding(padding).then(
            if (enableBorder) Modifier.border(
                width = borderWidth,
                radius = 15.px,
                color = Colors.primaryDark
            ) else Modifier
        ),
    ) {
        MaterialIcon(
            iconCode = "info",
            modifier = Modifier.color(Colors.primaryDark)
                .then(if (iconSize != null) Modifier.fontSize(iconSize) else Modifier)
        )
        Spacer(modifier = Modifier.height(5.px))
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = Constants.Inter,
            color = Colors.primaryDark,
            fontWeight = fontWeight
        )
    }
    Spacer(modifier = Modifier.height(25.px))
}