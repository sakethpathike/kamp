package sakethh.kamp.domain.model.markdown

sealed interface MarkdownNode{
    data class Heading(val level: Int, val text: String) : MarkdownNode
    data class Paragraph(val inlineNodes: List<InlineNode>) : MarkdownNode
    data class Quote(val inlineNodes: List<InlineNode>) : MarkdownNode
    data class CodeBlock(val text: String) : MarkdownNode
    data class ListItem(val inlineNodes: List<InlineNode>) : MarkdownNode
    data class Image(val src: String, val altText: String) : MarkdownNode
    data object Divider : MarkdownNode
}
