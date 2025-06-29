package sakethh.kamp.presentation.blog

import kotlinx.html.BODY
import kotlinx.html.onClick
import sakethh.kamp.data.blog.MarkdownParser
import sakethh.kamp.domain.model.BlogItem
import sakethh.kamp.domain.model.markdown.EmphasisType
import sakethh.kamp.domain.model.markdown.InlineNode
import sakethh.kamp.domain.model.markdown.MarkdownNode
import sakethh.kamp.presentation.common.Footer
import sakethh.kamp.presentation.common.Header
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kamp.presentation.utils.blockSelection
import sakethh.kamp.presentation.utils.encodeForHtml
import sakethh.kapsule.*
import sakethh.kapsule.utils.*
import java.util.*

fun BODY.BlogPage(fileName: String) {
    Column(
        id = "current_page", modifier = Modifier.padding(50.px).fillMaxWidth(0.7)
    ) {
        Header(selectedComponent = "blog")
        Spacer(modifier = Modifier.height(25.px))

        val blogItem = BlogItem.getBlogItem(fileName)

        Text(
            text = blogItem.blogName,
            color = Colors.primaryDark,
            fontWeight = FontWeight.Predefined.Bold,
            fontFamily = Constants.Inter,
            fontSize = 24.px
        )

        if (blogItem.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(5.px))
            Text(
                text = blogItem.description,
                color = Colors.secondaryDark,
                fontWeight = FontWeight.Predefined.Medium,
                fontFamily = Constants.Inter,
                fontSize = 16.px
            )
        }
        Spacer(modifier = Modifier.height(5.px))
        Row(horizontalAlignment = HorizontalAlignment.Center, modifier = Modifier.width("fit-content")) {
            Span(
                modifier = Modifier.color(Colors.secondaryDark).fontSize(16.px),
                className = "material-symbols-outlined",
                onThisElement = {}) {
                +"calendar_clock"
            }
            Spacer(modifier = Modifier.width(5.px))
            Text(
                text = blogItem.pubDateTime,
                color = Colors.secondaryDark,
                fontWeight = FontWeight.Predefined.Normal,
                fontFamily = Constants.Inter,
                fontSize = 14.px
            )
        }
        Spacer(modifier = Modifier.height(10.px))
        Text(
            text = """
                The following rendered output is parsed using a custom parser based on the <a target="_blank" style = "color: ${Colors.primaryDark}" href="https://spec.commonmark.org/0.31.2/#appendix-a-parsing-strategy">CommonMark spec</a>.
            """.trimIndent(),
            fontSize = 14.px,
            fontFamily = Constants.Inter,
            color = Colors.primaryDark,
            fontWeight = FontWeight.Predefined.Thin
        )
        Spacer(modifier = Modifier.height(25.px))
        MarkdownParser().mdToHtml(blogItem.rawFileContent.substringAfter("pubDatetime").substringAfter("---").trim())
            .forEach {
                when (it) {
                    is MarkdownNode.CodeBlock -> {
                        val currentTextId = UUID.randomUUID().toString()
                        Column(
                            modifier = Modifier.clip(Shape.RoundedRectangle(cornerRadius = 15.px)).backgroundColor(
                                Colors.codeblockBG
                            ).border(radius = 15.px, color = Colors.primaryContainerDark, width = 2.75.px)
                                .margin(top = 10.px, bottom = 10.px)
                        ) {
                            Text(
                                id = currentTextId,
                                text = "<pre>${it.text.encodeForHtml()}</pre>",
                                fontWeight = FontWeight.Predefined.Medium,
                                color = Colors.primaryDark,
                                fontSize = 16.px,
                                modifier = Modifier.padding(12.px).custom("overflow: auto; ")
                            )
                            Box(modifier = Modifier.backgroundColor(Colors.primaryContainerDark)) {
                                MaterialIcon(
                                    iconCode = "content_copy",
                                    modifier = Modifier.margin(10.px).blockSelection().cursor(Cursor.Pointer)
                                        .fontSize(16.px).color(Colors.primaryDark),
                                    onThisElement = {
                                        onClick = """
                                            const currentCodeBlock = document.getElementById('${currentTextId}');
                                            const textToCopy = currentCodeBlock.innerText;

                                            if (navigator.clipboard && navigator.clipboard.writeText) {
                                              navigator.clipboard.writeText(textToCopy)
                                                .then(() => console.log('Copied to clipboard!'))
                                                .catch(err => alert('Copy failed: ' + err));
                                            } else {
                                              const textarea = document.createElement('textarea');
                                              textarea.value = textToCopy;
                                              textarea.style.position = 'absolute';
                                              textarea.style.left = '-9999px';
                                              document.body.appendChild(textarea);
                                              textarea.select();
                                              try {
                                                document.execCommand('copy');
                                                console.log('Copied via execCommand');
                                              } catch (err) {
                                                alert('Fallback copy failed: ' + err);
                                              }
                                              document.body.removeChild(textarea);
                                            }
                                        """.trimIndent()
                                    })
                            }
                        }
                    }

                    MarkdownNode.Divider -> {
                        Row(
                            verticalAlignment = VerticalAlignment.Center,
                            horizontalAlignment = HorizontalAlignment.Center
                        ) {
                            Spacer(
                                modifier = Modifier.fillMaxWidth(0.98)
                                    .border(radius = 5.px, color = Colors.codeblockBG, width = 1.15.px)
                                    .backgroundColor(Colors.codeblockBG).margin(top = (3.5 + 5).px, bottom = 5.px)
                            )
                        }
                    }

                    is MarkdownNode.Heading -> {
                        Heading(
                            level = it.level,
                            text = it.text,
                            modifier = Modifier.fontFamily(Constants.Inter).color(Colors.primaryDark)
                        )
                    }

                    is MarkdownNode.Quote, is MarkdownNode.ListItem, is MarkdownNode.Paragraph -> {
                        val currentMarkdownNode = it
                        val inlineNodes = when (it) {
                            is MarkdownNode.ListItem -> it.inlineNodes
                            is MarkdownNode.Paragraph -> it.inlineNodes
                            is MarkdownNode.Quote -> it.inlineNodes
                            else -> error("It SHOULD NOT be here")
                        }
                        Box(modifier = Modifier.position(Position.Relative)) {
                            if (it is MarkdownNode.Quote) {
                                Column(
                                    verticalAlignment = VerticalAlignment.Center,
                                    modifier = Modifier.fillMaxSize().backgroundColor(Colors.codeblockBG)
                                        .position(Position.Absolute).zIndex(-1).custom("top: 0; left: 0;")
                                ) {
                                    Spacer(
                                        modifier = Modifier.margin(start = 5.px).fillMaxHeight(0.8).width(4.px)
                                            .clip(Shape.RoundedRectangle(cornerRadius = 5.px))
                                            .backgroundColor(Colors.primaryDark)
                                    )
                                }
                            }
                            Column(modifier = if (it is MarkdownNode.Quote) Modifier.margin(start = 10.px) else Modifier) {
                                Span(
                                    onThisElement = {}, modifier = Modifier.margin(top = 2.5.px, bottom = 2.5.px).fontSize(0.px)
                                ) {
                                    inlineNodes.forEach {
                                        when (it) {
                                            is InlineNode.CodeSpan -> {
                                                Text(
                                                    text = it.code.trim(),
                                                    modifier = Modifier.backgroundColor(Colors.onPrimaryDark)
                                                        .borderRadius(4.px).color(Colors.primaryDark)
                                                        .custom("padding:2px 4px;").fontFamily(Constants.JetBrainsMono)
                                                        .width("fit-content").display(Display.Inline),
                                                    fontSize = 15.px
                                                )
                                            }

                                            is InlineNode.Emphasis -> {
                                                Text(
                                                    text = it.text.run {
                                                        if (it.type == EmphasisType.BoldItalic || it.type == EmphasisType.Italic) {
                                                            "<i>${it.text}</i>"
                                                        } else if (it.type == EmphasisType.StrikeThrough) {
                                                            "<del>${it.text}</del>"
                                                        } else {
                                                            this
                                                        }
                                                    }.trim(),
                                                    fontSize = 18.px,
                                                    color = Colors.onSurfaceDark,
                                                    fontFamily = Constants.Inter,
                                                    fontWeight = when (it.type) {
                                                        EmphasisType.StrikeThrough, EmphasisType.Italic -> FontWeight.Predefined.Normal
                                                        EmphasisType.BoldItalic, EmphasisType.Bold -> FontWeight.Predefined.Bold
                                                    },
                                                    modifier = Modifier.width("fit-content").display(Display.Inline)
                                                )
                                            }

                                            is InlineNode.Link -> {
                                                Text(
                                                    text = """
                         <a style = "color: ${Colors.primaryDark}" href="${it.url}" target="_blank">${it.text}</a>
                                            """.trimIndent(),
                                                    fontSize = 18.px,
                                                    fontFamily = Constants.Inter,
                                                    fontWeight = FontWeight.Predefined.SemiBold,
                                                    modifier = Modifier.width("fit-content").display(Display.Inline)
                                                )
                                            }

                                            is InlineNode.PlainText -> {
                                                Text(
                                                    text = it.text.run {
                                                        if (currentMarkdownNode is MarkdownNode.ListItem && this.startsWith(
                                                                "-"
                                                            )
                                                        ) {
                                                            Typography.bullet + " " + this.substring(1)
                                                        } else {
                                                            this
                                                        }
                                                    }.encodeForHtml(),
                                                    fontSize = 18.px,
                                                    color = Colors.onSurfaceDark,
                                                    fontFamily = Constants.Inter,
                                                    fontWeight = FontWeight.Predefined.Normal,
                                                    modifier = Modifier.width("fit-content").display(Display.Inline)
                                                        .custom("line-height: 1.5; white-space: pre-wrap; word-break: break-word;")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is MarkdownNode.Image -> {
                        Image(
                            src = it.src,
                            modifier = Modifier.margin(top = 5.px, bottom = 5.px)
                                .border(radius = 15.px, color = Colors.primaryContainerDark, width = 2.75.px)
                        )
                    }
                }
            }
        Footer()
    }
}