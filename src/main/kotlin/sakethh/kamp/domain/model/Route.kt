package sakethh.kamp.domain.model

import kotlinx.html.BODY

data class Route(
    val route: String, val metaTags: MetaTags, val content: (BODY) -> Unit
)
