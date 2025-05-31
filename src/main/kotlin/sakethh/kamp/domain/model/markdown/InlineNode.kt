package sakethh.kamp.domain.model.markdown

sealed interface InlineNode {
    data class PlainText(val text: String) : InlineNode
    data class Emphasis(val type: EmphasisType, val children: List<InlineNode>) : InlineNode
    data class CodeSpan(val code: String) : InlineNode
    data class Link(val children: List<InlineNode>, val url: String) : InlineNode
}

enum class EmphasisType { Italic, Bold, StrikeThrough }
