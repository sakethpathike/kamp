package sakethh.kamp.data.blog

import sakethh.kamp.domain.model.markdown.EmphasisType
import sakethh.kamp.domain.model.markdown.InlineNode
import sakethh.kamp.domain.model.markdown.MarkdownNode

// https://spec.commonmark.org/0.31.2/#appendix-a-parsing-strategy
class MarkdownParser {

    fun mdToHtml(blogContent: String): List<MarkdownNode> {
        return figureOutTheLayout(blogContent)
    }

    private var skipUntilLineNumber: Int? = null
    private var paragraphBuilder: StringBuilder? = null

    // phase 1
    private fun figureOutTheLayout(blogContent: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val allLines = blogContent.split('\n')
        allLines.forEachIndexed { currentLineNumber, currentLineContent ->
            if (skipUntilLineNumber != null && skipUntilLineNumber!! < currentLineNumber) skipUntilLineNumber = null
            if (skipUntilLineNumber != null && currentLineNumber < skipUntilLineNumber!!) return@forEachIndexed

            val trimmedLineContent = currentLineContent.trimStart()
            val leadingSpaceExists = (currentLineContent.length - trimmedLineContent.length) <= 3

            when {

                currentLineContent.isBlank() -> {
                    if (paragraphBuilder != null && paragraphBuilder!!.isNotBlank()) {
                        nodes.add(MarkdownNode.Paragraph(inlineNodes = processInlineElements(paragraphBuilder.toString())))
                    }
                    paragraphBuilder = null
                }

                trimmedLineContent.startsWith(">") -> nodes.add(
                    MarkdownNode.Quote(
                        inlineNodes = processInlineElements(currentLineContent.substringAfter(">").trim())
                    )
                )

                trimmedLineContent.startsWith("```") -> nodes.add(
                    MarkdownNode.CodeBlock(
                        text = allLines.subList(
                            currentLineNumber + 1,
                            currentLineNumber + 1 + allLines.subList(currentLineNumber + 1, allLines.size)
                                .indexOfFirst {
                                    it == "```"
                                }.also {
                                    skipUntilLineNumber = currentLineNumber + 1 + it + 1 // we also need
                                    // to keep track of previous lines
                                    // which are not included in this sublist
                                }).joinToString(separator = "\n")
                    )
                )

                leadingSpaceExists && (trimmedLineContent.startsWith("- ") || trimmedLineContent.startsWith(
                    "+ "
                ) || Regex("""^\d+\.\s""").containsMatchIn(trimmedLineContent)) -> nodes.add(
                    MarkdownNode.ListItem(
                        inlineNodes = processInlineElements(currentLineContent)
                    )
                )


                trimmedLineContent.startsWith("---") || trimmedLineContent.startsWith("___") || trimmedLineContent.startsWith(
                    "***"
                ) -> nodes.add(
                    MarkdownNode.Divider
                )

                Regex("^#{1,6}\\s").containsMatchIn(trimmedLineContent) -> nodes.add(MarkdownNode.Heading(level = trimmedLineContent.takeWhile {
                    it == '#'
                }.length.coerceAtMost(6), text = trimmedLineContent.substringAfter(" ").trim()))

                trimmedLineContent.startsWith("![") -> nodes.add(
                    MarkdownNode.Image(
                        src = trimmedLineContent.substringAfter("(").substringBefore(")"),
                        altText = trimmedLineContent.substringAfter("![").substringBefore("](")
                    )
                )

                else -> {
                    if (paragraphBuilder == null) {
                        paragraphBuilder = StringBuilder()
                    } else {
                        paragraphBuilder?.append(" ")
                    }
                    paragraphBuilder?.append(trimmedLineContent)
                }
            }
        }

        // if there is any normal text, that's part of the paragraph or just a line
        if (paragraphBuilder != null && paragraphBuilder!!.isNotBlank()) {
            nodes.add(MarkdownNode.Paragraph(inlineNodes = processInlineElements(paragraphBuilder.toString())))
            paragraphBuilder!!.clear()
        }
        return nodes.toList()
    }

    // phase 2
    private fun processInlineElements(string: String): List<InlineNode> {
        val inlineElements = mutableListOf<InlineNode>()
        var skipUntilIndex: Int = -1
        val tempPlainText = StringBuilder()
        val fences = listOf('*', '_', '`', '[', ']', '(', ')', '~')
        string.forEachIndexed { index, currentChar ->
            if (index < skipUntilIndex) return@forEachIndexed

            if (currentChar in fences && tempPlainText.isNotEmpty()) {
                inlineElements.add(InlineNode.PlainText(tempPlainText.toString()))
                tempPlainText.clear()
            }

            when (currentChar) {
                '`' -> {
                    inlineElements.add(
                        InlineNode.CodeSpan(
                            code = string.substring(startIndex = index + 1).substringBefore("`")
                        )
                    )
                    skipUntilIndex = string.skipUntil(targetChar = '`', currentIndex = index)
                }

                '[' -> {
                    inlineElements.add(
                        InlineNode.Link(
                            url = string.substring(startIndex = index).substringAfter("](").substringBefore(")"),
                            text = string.substring(startIndex = index + 1).substringBefore("]")
                        )
                    )
                    skipUntilIndex = string.skipUntil(targetChar = ')', currentIndex = index)
                }

                '*' -> {
                    if (string[index + 1] == '*') {
                        tempPlainText.append("**")
                        skipUntilIndex = index + 2
                        return@forEachIndexed
                    }
                    // BOLD
                    if (string[index + 1] !in fences) {
                        inlineElements.add(
                            InlineNode.Emphasis(
                                type = EmphasisType.Bold,
                                text = string.substring(startIndex = index + 1).substringBefore("*")
                            )
                        )
                        skipUntilIndex = string.skipUntil(targetChar = '*', currentIndex = index)
                    }

                    // BOLD_ITALIC
                    if (string[index + 1] == '_') {
                        inlineElements.add(
                            InlineNode.Emphasis(
                                type = EmphasisType.BoldItalic,
                                text = string.substring(startIndex = index + 2).substringBefore("_*")
                            )
                        )
                        skipUntilIndex = string.skipUntil(targetChar = '*', currentIndex = index)
                    }
                }

                '_' -> {
                    if (string[index + 1] == '*') {
                        tempPlainText.append("_*")
                        skipUntilIndex = index + 2
                        return@forEachIndexed
                    }
                    inlineElements.add(
                        InlineNode.Emphasis(
                            type = EmphasisType.Italic,
                            text = string.substring(startIndex = index + 1).substringBefore("_")
                        )
                    )
                    skipUntilIndex = string.skipUntil(targetChar = '_', currentIndex = index)
                }

                else -> {
                    if (currentChar == '~' && string[index + 1] == '~') {
                        inlineElements.add(
                            InlineNode.Emphasis(
                                type = EmphasisType.StrikeThrough,
                                text = string.substring(startIndex = index + 2).substringBefore("~")
                            )
                        )
                        skipUntilIndex = string.substring(startIndex = index + 2).indexOfFirst {
                            it == '~'
                        } + 4 + index

                        return@forEachIndexed
                    }
                    tempPlainText.append(currentChar)
                }
            }
        }
        inlineElements.add(InlineNode.PlainText(tempPlainText.toString()))
        tempPlainText.clear()
        return inlineElements
    }

    private fun String.skipUntil(targetChar: Char, currentIndex: Int): Int {
        return this.substring(startIndex = currentIndex + 1).indexOfFirst {
            it == targetChar
        } + 2 + currentIndex
    }
}