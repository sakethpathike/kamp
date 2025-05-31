package sakethh.kamp.data.blog

import sakethh.kamp.domain.model.*

// https://spec.commonmark.org/0.31.2/#appendix-a-parsing-strategy
object MarkdownParser {

    fun mdToHtml(blogContent: String): List<MarkdownNode> {
        return figureOutTheLayout(blogContent)
    }

    private var skipUntilLineNumber: Int? = null

    // phase 1
    private fun figureOutTheLayout(blogContent: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val allLines = blogContent.split('\n')
        allLines.forEachIndexed { currentLineNumber, currentLineContent ->
            if (skipUntilLineNumber != null && skipUntilLineNumber!! < currentLineNumber) skipUntilLineNumber = null
            if (skipUntilLineNumber != null && currentLineNumber < skipUntilLineNumber!!) return@forEachIndexed

            val trimmedLine = currentLineContent.trimStart()
            val leadingSpaceExists = (currentLineContent.length - trimmedLine.length) <= 3

            when {
                trimmedLine.startsWith(">") -> nodes.add(Quote(text = currentLineContent.substringAfter(">").trim()))

                trimmedLine.startsWith("```") -> nodes.add(
                    CodeBlock(
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

                leadingSpaceExists && (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") || trimmedLine.startsWith(
                    "+ "
                ) || Regex("""^\d+\.\s""").containsMatchIn(trimmedLine)) -> nodes.add(ListItem(text = currentLineContent))


                trimmedLine.startsWith("---") || trimmedLine.startsWith("___") || trimmedLine.startsWith("***") -> nodes.add(
                    Divider
                )

                Regex("^#{1,6}\\s").containsMatchIn(trimmedLine) -> nodes.add(Heading(level = trimmedLine.takeWhile {
                    it == '#'
                }.length.coerceAtMost(6), text = trimmedLine.substringAfter(" ").trim()))

                Regex("^\\[[^]]+]:\\s*\\S+\\s*$").containsMatchIn(trimmedLine) -> nodes.add(Link(currentLineContent))

                trimmedLine.startsWith("![") -> nodes.add(Image(src = trimmedLine.substringAfter("(").substringBefore(")"), altText = trimmedLine.substringAfter("![").substringBefore("](")))

                else -> nodes.add(Text(value = currentLineContent))
            }
        }
        return nodes.toList()
    }
}