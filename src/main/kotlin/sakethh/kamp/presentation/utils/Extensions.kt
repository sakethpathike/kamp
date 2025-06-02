package sakethh.kamp.presentation.utils

import sakethh.kapsule.Modifier
import sakethh.kapsule.custom

fun Modifier.blockSelection() = this.custom(
    """
          user-select: none;
        """.trimIndent()
)

fun String.encodeForHtml(): String {
    return this.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}