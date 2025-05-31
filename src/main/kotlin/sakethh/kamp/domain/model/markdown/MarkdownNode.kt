package sakethh.kamp.domain.model.markdown

sealed interface MarkdownNode{
    data class Heading(val level: Int, val text: String) : MarkdownNode
    data class Text(val inlineNodes: List<InlineNode>) : MarkdownNode
    data class Quote(val text: String) : MarkdownNode
    data class CodeBlock(val text: String) : MarkdownNode
    data class ListItem(val text: String) : MarkdownNode
    data class Link(val text: String) : MarkdownNode
    data class Image(val src: String, val altText: String) : MarkdownNode
    data object Divider : MarkdownNode
}
