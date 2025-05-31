package sakethh.kamp.presentation.common

import kotlinx.html.FlowContent
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kapsule.Column
import sakethh.kapsule.MaterialIcon
import sakethh.kapsule.Modifier
import sakethh.kapsule.Spacer
import sakethh.kapsule.Text
import sakethh.kapsule.border
import sakethh.kapsule.color
import sakethh.kapsule.height
import sakethh.kapsule.padding
import sakethh.kapsule.utils.FontWeight
import sakethh.kapsule.utils.px

fun FlowContent.Footer(){
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