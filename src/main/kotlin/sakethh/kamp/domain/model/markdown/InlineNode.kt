package sakethh.kamp.domain.model.markdown

sealed interface InlineNode {
    data class PlainText(val text: String) : InlineNode
    data class Emphasis(val type: EmphasisType, val text: String) : InlineNode
    data class CodeSpan(val code: String) : InlineNode
    data class Link(val url: String, val text: String) : InlineNode
}

enum class EmphasisType { Italic, Bold, BoldItalic, StrikeThrough }
