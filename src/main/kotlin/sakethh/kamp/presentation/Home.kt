package sakethh.kamp.presentation

import kotlinx.html.BODY
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kapsule.Text

fun BODY.Home() {
    Text(text = "Wave Gods", fontFamily = Constants.Inter, color = Colors.OnBackground)
}