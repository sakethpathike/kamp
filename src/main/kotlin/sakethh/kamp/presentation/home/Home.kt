package sakethh.kamp.presentation.home

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import sakethh.kamp.domain.model.GithubRepoDTO
import sakethh.kamp.presentation.common.Footer
import sakethh.kamp.presentation.common.Header
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kamp.presentation.utils.blockSelection
import sakethh.kapsule.*
import sakethh.kapsule.Row
import sakethh.kapsule.Spacer
import sakethh.kapsule.utils.*

fun BODY.Home() {
    script(type = ScriptType.textJavaScript) {
        unsafe {
            +"""
        document.addEventListener("DOMContentLoaded", async () => {
            const starEls = document.querySelectorAll('[id$="|starCount"]');
            const descEls = document.querySelectorAll('[id$="|desc"]');

            const repoMap = new Map();
            const userSet = new Set();
            const orgSet = new Set();

            [...starEls, ...descEls].forEach(el => {
                const key = el.id.replace(/\|(starCount|desc)$/, "");
                if (key.includes("/")) orgSet.add(key.split("/")[0]);
                else userSet.add("sakethpathike");
            });

            for (const user of userSet) {
                try {
                    const res = await fetch(`https://api.github.com/users/${'$'}{user}/repos`);
                    if (!res.ok) continue;
                    const data = await res.json();
                    data.forEach(r => repoMap.set(`${'$'}{user}/${'$'}{r.name}`, r));
                } catch {}
            }

            for (const org of orgSet) {
                try {
                    const res = await fetch(`https://api.github.com/orgs/${'$'}{org}/repos`);
                    if (!res.ok) continue;
                    const data = await res.json();
                    data.forEach(r => repoMap.set(`${'$'}{org}/${'$'}{r.name}`, r));
                } catch {}
            }

            starEls.forEach(el => {
                const key = el.id.replace(/\|starCount$/, "");
                const full = key.includes("/") ? key : `sakethpathike/${'$'}{key}`;
                const repo = repoMap.get(full);
                if (repo) el.textContent = repo.stargazers_count;
            });

            descEls.forEach(el => {
                const key = el.id.replace(/\|desc$/, "");
                const full = key.includes("/") ? key : `sakethpathike/${'$'}{key}`;
                const repo = repoMap.get(full);
                if (repo && repo.description) el.textContent = repo.description;
            });
        });
    """.trimIndent()
        }
    }

    Column(
        id = "current_page",
        modifier = Modifier.pageMargin().fillMaxWidth(0.7)
    ) {
        Header(selectedComponent = "home")
        Spacer(modifier = Modifier.height(20.px))
        Text(
            fontFamily = Constants.Inter,
            text = """
               Hey, I'm Saketh. I build Android apps and Kotlin Multiplatform projects.
            """.trimIndent(),
            fontWeight = FontWeight.Custom("400"),
            fontSize = 18.px,
            color = Colors.secondaryDark,
            modifier = Modifier.custom("line-height:1.4; ")
        )
        Spacer(modifier = Modifier.height(4.px))
        Text(
            fontFamily = Constants.Inter,
            text = """
            I build my own tools when I need them, like <a style="color: ${Colors.primaryDark}" href="https://github.com/sakethpathike/kamp" target="_blank">kamp</a> and <a style="color: ${Colors.primaryDark}" href="https://github.com/sakethpathike/kapsule" target="_blank">kapsule</a>, which power this site.        """.trimIndent(),
            fontWeight = FontWeight.Custom("400"),
            fontSize = 18.px,
            color = Colors.secondaryDark,
            modifier = Modifier.custom("line-height:1.4; ")
        )
        Spacer(modifier = Modifier.height(4.px))
        divider()
        Text(
            text = "Contact",
            fontWeight = FontWeight.Predefined.Bold,
            color = Colors.primaryDark,
            fontSize = 24.px,
            fontFamily = Constants.Inter
        )
        Spacer(modifier = Modifier.height(10.px))
        ContactItem(
            text = "<a style=\"color: ${Colors.primaryDark}\" href=\"mailto:sakethpathike@gmail.com\" target=\"_blank\">sakethpathike@gmail.com</a>",
            fontWeight = FontWeight.Predefined.SemiBold
        )
        Spacer(modifier = Modifier.height(10.px))

        Row(
            modifier = Modifier.display(Display.Flex).custom(
                """
            flex-wrap: wrap; gap: 10px;
        """.trimIndent()
            )
        ) {
            ContactItem(text = "<a style=\"color: ${Colors.primaryDark}\" href=\"https://github.com/sakethpathike\" target=\"_blank\">Github</a>")
            ContactItem(text = Typography.bullet.toString())
            ContactItem(text = "<a style=\"color: ${Colors.primaryDark}\" href=\"https://www.linkedin.com/in/sakethpathike/\" target=\"_blank\">LinkedIn</a>")
            ContactItem(text = Typography.bullet.toString())
            ContactItem(text = "<a style=\"color: ${Colors.primaryDark}\" href=\"https://x.com/sakethpathike/\" target=\"_blank\">Twitter/X</a>")
        }
        Spacer(modifier = Modifier.height(20.px))
        Text(
            text = "Projects",
            fontWeight = FontWeight.Predefined.Bold,
            color = Colors.primaryDark,
            fontSize = 24.px,
            fontFamily = Constants.Inter
        )
        Spacer(modifier = Modifier.height(5.px))
        Text(
            fontFamily = Constants.Inter,
            text = "Data includes information from my pinned GitHub repositories and project tags.",
            fontWeight = FontWeight.Predefined.Light,
            fontSize = 14.px,
            color = Colors.primaryDark
        )
        Spacer(modifier = Modifier.height(15.px))
        getPinnedRepos().forEach {
            RepoItem(githubRepoDTO = it)
            Spacer(modifier = Modifier.height(15.px))
        }
        divider()
    }
}

fun FlowContent.divider(){
    Row(
        verticalAlignment = VerticalAlignment.Center, horizontalAlignment = HorizontalAlignment.Center
    ) {
        Spacer(
            modifier = Modifier.fillMaxWidth(0.98)
                .border(radius = 5.px, color = Colors.codeblockBG, width = 1.15.px)
                .backgroundColor(Colors.codeblockBG).opacity(0.45)
                .margin(start = 7.5.px, end = 7.5.px, top = 15.px, bottom = 15.px)
        )
    }
}
private fun FlowContent.RepoItem(githubRepoDTO: GithubRepoDTO) {
    Column {
        Row(
            horizontalAlignment = HorizontalAlignment.Center,
            modifier = Modifier.cursor(Cursor.Pointer),
            onThisElement = {
                val func =
                    "window.open(\"https://github.com/${if (githubRepoDTO.name.contains("/")) githubRepoDTO.name else "sakethpathike/${githubRepoDTO.name}"}\", \"_blank\");"
                onMouseDown = func
            }) {
            MaterialIcon(iconCode = "open_in_new", modifier = Modifier.color(Colors.primaryDark).fontSize(14.px))
            Spacer(modifier = Modifier.width(2.5.px))
            Text(
                text = githubRepoDTO.name,
                color = Colors.primaryDark,
                fontSize = 18.px,
                fontWeight = FontWeight.Predefined.Bold,
                fontFamily = Constants.Inter
            )
        }
        Spacer(modifier = Modifier.height(5.px))
        Text(
            text = githubRepoDTO.description,
            fontSize = 16.px,
            color = Colors.secondaryDark,
            fontFamily = Constants.Inter,
            id = githubRepoDTO.name + "|desc"
        )
        Spacer(modifier = Modifier.height(5.px))
        Row(
            horizontalAlignment = HorizontalAlignment.Center,
        ) {
            MaterialIcon(
                iconCode = "star_outline",
                modifier = Modifier.fontSize(18.px).color(Colors.secondaryDark).blockSelection()
            )
            Spacer(modifier = Modifier.width(5.px))
            Text(
                text = githubRepoDTO.starCount,
                fontFamily = Constants.Inter,
                color = Colors.secondaryDark,
                fontSize = 14.px,
                id = githubRepoDTO.name + "|starCount"
            )
        }
        Spacer(modifier = Modifier.height(5.px))
        Row(
            modifier = Modifier.display(Display.Flex).custom(
                """
            flex-wrap: wrap; gap: 5px;
        """.trimIndent()
            )
        ) {
            githubRepoDTO.tags.forEach {
                Text(
                    text = it,
                    color = Colors.secondaryContainerDarkMediumContrast,
                    fontWeight = FontWeight.Predefined.Normal,
                    fontFamily = Constants.Inter,
                    fontSize = 12.5.px,
                    modifier = Modifier.padding(2.5.px)
                )
            }
        }
    }
}

fun Modifier.pageMargin() = this.margin(start = 50.px, end = 50.px, top = 45.px, bottom = 50.px)

private fun FlowContent.ContactItem(
    text: String, fontWeight: FontWeight = FontWeight.Predefined.Medium, fontSize: String = 18.px
) {
    Text(
        fontFamily = Constants.Inter,
        text = text,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = Colors.secondaryDark,
        modifier = Modifier.custom("line-height:1.4; ")
    )
}

// as of now this is directly used in the presentation layer, ig that's not how it works (according to clean arch 🤓☝️)
fun getPinnedRepos(): List<GithubRepoDTO> = runBlocking {
    val pinnedRepos = mutableListOf<GithubRepoDTO>()
    HttpClient(CIO).use {
        it.get("https://github.com/sakethpathike").bodyAsText().let {
            it.substringAfter("<div class=\"js-pinned-items-reorder-container\">").substringAfter("<ol")
                .substringBefore("</ol>").split("<li").drop(1).forEach {
                    val pinnedItem =
                        it.substringAfter("<div class=\"pinned-item-list-item-content\">").substringAfter("</a>")

                    val name = pinnedItem.substringAfter("<div class=\"d-flex width-full position-relative\">")
                        .substringBefore("<p class=\"pinned-item-desc").substringAfter("<tool-tip")
                        .substringAfter("\">").substringBefore("</tool-tip>").trim()

                    val description = pinnedItem.substringAfter("<p class=\"pinned-item-desc").substringAfter("\">")
                        .substringBefore("</p>").trim()

                    val starCount = pinnedItem.substringAfter("<svg aria-label=\"stars\"").substringAfter("</svg>")
                        .substringBefore("</a>").trim()

                    val programmingLanguage =
                        pinnedItem.substringAfter("<span itemprop=\"programmingLanguage\">").substringBefore("</span>")
                            .trim()
                    pinnedRepos.add(
                        GithubRepoDTO(
                            name = name, description = description, starCount = try {
                                starCount.trim().toInt()
                            } catch (_: Exception) {
                                0
                            }.toString(), programmingLanguage = programmingLanguage, tags = tags.find {
                                it.repoName == name
                            }?.tags ?: emptyList()
                        )
                    )
                }
        }
    }
    pinnedRepos.toList()
}

data class Tags(val repoName: String, val tags: List<String>)

private val tags = listOf(
    Tags(
        repoName = "LinkoraApp/Linkora", tags = listOf(
            Constants.Kotlin,
            Constants.KMP,
            Constants.CMP,
            Constants.ANDROID_SDK,
            Constants.ANDROID_JETPACK,
            Constants.KtorClient,
            Constants.RealTimeSync
        )
    ), Tags(
        repoName = "LinkoraApp/sync-server", tags = listOf(
            Constants.Kotlin, Constants.KtorServer, Constants.Backend, Constants.Exposed, Constants.RealTimeSync
        )
    ), Tags(
        repoName = "kapsule", tags = listOf(
            Constants.Kotlin, Constants.KMP, Constants.kotlinxHtml, Constants.KT_DSL, Constants.MavenCentral
        )
    ), Tags(
        repoName = "JetSpacer", tags = listOf(
            Constants.Kotlin, Constants.ANDROID_SDK, Constants.JetpackCompose, Constants.KtorClient
        )
    ), Tags(
        repoName = "kamp", tags = listOf(
            Constants.Kotlin, Constants.KtorServer, Constants.Backend, Constants.Markdown, Constants.GitHubPages
        )
    ), Tags(
        repoName = "genesis", tags = listOf(
            Constants.Kotlin, Constants.SSG, Constants.CLI, Constants.KotlinScripting, Constants.MavenCentral
        )
    )
)