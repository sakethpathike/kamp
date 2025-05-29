package sakethh.kamp.domain.model

import kotlinx.html.BODY

data class Route(val route: String, val content: (BODY) -> Unit)
