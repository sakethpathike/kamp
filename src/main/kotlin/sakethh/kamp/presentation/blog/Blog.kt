package sakethh.kamp.presentation.blog

import kotlinx.html.BODY
import sakethh.kamp.data.blog.MarkdownParser
import sakethh.kamp.domain.model.*
import sakethh.kamp.presentation.common.Footer
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kamp.presentation.utils.blockSelection
import sakethh.kapsule.*
import sakethh.kapsule.utils.FontWeight
import sakethh.kapsule.utils.HorizontalAlignment
import sakethh.kapsule.utils.Shape
import sakethh.kapsule.utils.px

fun BODY.Blog(fileName: String) {
    Column(
        id = "current_page", modifier = Modifier.padding(50.px).fillMaxWidth(0.7)
    ) {
        Text(
            text = "Saketh",
            fontWeight = FontWeight.Predefined.SemiBold,
            fontSize = 14.px,
            fontFamily = Constants.Inter,
            color = Colors.secondaryContainerDark
        )
        Spacer(modifier = Modifier.height(10.px))
        Span(onThisElement = {}) {
            InlineCode(
                code = "Blog.kt",
                modifier = Modifier.backgroundColor(Colors.primaryDark).borderRadius(4.px).color(Colors.onPrimaryDark)
                    .custom("padding:2px 4px;").fontFamily(Constants.JetBrainsMono).blockSelection()
            )
        }
        Spacer(modifier = Modifier.height(20.px))

        val blogFile = object {}.javaClass.getResource("/blog/$fileName.md")!!
        val blogMeta = blogFile.readText().substringAfter("---").substringBefore("---").trim()
        val blogTitle = blogMeta.substringAfter("title:").substringBefore("\n").trim()
        val blogDescription = if (blogMeta.substringAfter("---").substringBefore("---").split("\n")[1].trim()
                .startsWith("description")
        ) blogMeta.substringAfter("description:").substringBefore("\n").trim() else ""
        val blogPubDateTime = blogMeta.substringAfter("pubDatetime:").substringBefore("\n").trim()

        Text(
            text = blogTitle,
            color = Colors.primaryDark,
            fontWeight = FontWeight.Predefined.Bold,
            fontFamily = Constants.Inter,
            fontSize = 24.px
        )

        if (blogDescription.isNotBlank()) {
            Spacer(modifier = Modifier.height(5.px))
            Text(
                text = blogDescription,
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
                text = blogPubDateTime,
                color = Colors.secondaryDark,
                fontWeight = FontWeight.Predefined.Normal,
                fontFamily = Constants.Inter,
                fontSize = 14.px
            )
        }
        Spacer(modifier = Modifier.height(25.px))
        MarkdownParser.mdToHtml(blogFile.readText().substringAfter("pubDatetime").substringAfter("---").trim())
            .forEach {
                when (it) {
                    is CodeBlock -> {
                        Column(
                            modifier = Modifier.clip(Shape.RoundedRectangle(cornerRadius = 15.px)).backgroundColor(
                                Colors.codeblockBG
                            ).border(radius = 15.px, color = Colors.primaryContainerDark, width = 2.75.px)
                                .margin(top = 10.px, bottom = 10.px)
                        ) {
                            Text(
                                text = "<pre>${it.text}</pre>",
                                fontWeight = FontWeight.Predefined.Medium,
                                color = Colors.primaryDark,
                                fontSize = 16.px,
                                modifier = Modifier.padding(12.px).custom("overflow: auto; ")
                            )
                        }
                    }

                    Divider -> Spacer(
                        modifier = Modifier.border(radius = 5.px, color = Colors.outlineDark, width = 1.25.px)
                            .backgroundColor(Colors.outlineDark).margin(top = 15.px, bottom = 15.px)
                    )

                    is Heading -> {
                        Heading(
                            level = it.level,
                            text = it.text,
                            modifier = Modifier.fontFamily(Constants.Inter).color(Colors.primaryDark)
                        )
                    }

                    is Link -> {
                        Text(
                            text = "<this is link>${it.text}<this is link>",
                            fontWeight = FontWeight.Predefined.Normal,
                            color = Colors.onSurfaceDark,
                            fontSize = 20.px
                        )
                    }

                    is ListItem -> {
                        Text(
                            text = it.text.run {
                                if (this.startsWith("-")) {
                                    Typography.bullet + this.substring(startIndex = 1)
                                } else {
                                    this
                                }
                            },
                            fontWeight = FontWeight.Predefined.Normal,
                            color = Colors.onSurfaceDark,
                            fontSize = 18.px,
                            fontFamily = Constants.Inter,
                            modifier = Modifier.margin(top = 5.px, start = 5.px, end = 5.px)
                        )
                    }

                    is Quote -> {
                        Text(
                            text = "<i>${it.text}</i>",
                            fontWeight = FontWeight.Predefined.Black,
                            color = Colors.tertiaryDark,
                            fontSize = 18.px
                        )
                    }

                    is Text -> {
                        Text(
                            text = it.value,
                            fontSize = 18.px,
                            color = Colors.onSurfaceDark,
                            fontFamily = Constants.Inter,
                            fontWeight = FontWeight.Predefined.Normal,
                            modifier = Modifier.margin(top = 5.px, bottom = 5.px)
                        )
                    }

                    is Image -> {
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