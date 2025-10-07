package sakethh.kamp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import sakethh.kamp.domain.model.DeployTarget
import sakethh.kamp.domain.model.MetaTags
import sakethh.kamp.domain.model.Route
import sakethh.kamp.presentation.blog.BlogList
import sakethh.kamp.presentation.blog.BlogPage
import sakethh.kamp.presentation.home.Home
import sakethh.kamp.presentation.utils.Colors
import sakethh.kamp.presentation.utils.Constants
import sakethh.kamp.snapshot.SnapshotManager
import sakethh.kapsule.*
import sakethh.kapsule.utils.px
import java.net.Inet4Address
import kotlin.io.path.ExperimentalPathApi

fun main() {
    embeddedServer(
        Netty, port = 8080, host = Inet4Address.getLocalHost().hostAddress, module = Application::module
    ).start(wait = true)
}

val blogFileNames: List<String> = object {}.javaClass.getResourceAsStream("/blog/blogNames.txt")?.use {
    it.bufferedReader().use {
        it.readText().split(",")
    }.map { it.trim() }
} ?: emptyList()

@OptIn(ExperimentalPathApi::class)
fun Application.module() {
    install(CORS) {
        allowHost(host = "sakethpathike.github.io")
        allowHost(host = "sakethpathike.netlify.app")
        allowHost(host = "kamp.onrender.com")
    }
    install(Authentication) {
        bearer(name = Constants.BEARER_AUTH) {
            authenticate { authToken ->
                if (authToken.token == System.getenv(Constants.BEARER_AUTH)) {
                    UserIdPrincipal(name = "admin")
                } else {
                    null
                }
            }
        }
    }
    val allRoutes = mutableListOf(
        Route(
            route = "/", content = {
                with(it) {
                    this.Home()
                }
            }, metaTags = MetaTags.HomePage(deployTarget = DeployTarget.Koyeb)
        ),
        Route(
            route = "/blog", content = {
                with(it) {
                    this.BlogList()
                }
            }, metaTags = MetaTags.BlogListPage(deployTarget = DeployTarget.Koyeb)
        ),
    )
    blogFileNames.forEach { fileName ->
        allRoutes.add(
            Route(
                route = "/blog/${fileName}",
                metaTags = MetaTags.BlogPage(fileName = fileName, deployTarget = DeployTarget.Koyeb),
                content = {
                    with(it) {
                        BlogPage(fileName)
                    }
                })
        )
    }
    routing {
        authenticate(Constants.BEARER_AUTH) {
            get(path = "/snapshot/push") {
                call.respond(withContext(Dispatchers.IO) {
                    SnapshotManager.pushANewSnapshot().fold(onSuccess = { it }, onFailure = { it.stackTraceToString() })
                })
            }
        }
        staticResources(remotePath = "/", basePackage = "static")
        allRoutes.forEach { currentRoute ->
            get(currentRoute.route) {
                call.respondText(contentType = ContentType.Text.Html, text = createHTML().html {
                    KampSurface(metaTags = currentRoute.metaTags) {
                        currentRoute.content(this)
                    }
                })
            }
        }
    }
}

fun HTML.KampSurface(metaTags: MetaTags, content: BODY.() -> Unit) {
    Surface(
        onTheBodyElement = {
        unsafe {
            +"""
                        <script async src="https://www.googletagmanager.com/gtag/js?id=G-V84KZKD6BP"></script>
                        <script>
                          window.dataLayer = window.dataLayer || [];
                          function gtag(){dataLayer.push(arguments);}
                          gtag('js', new Date());

                          gtag('config', 'G-V84KZKD6BP');
                        </script>
                    """.trimIndent()
        }
        script(type = ScriptType.textJavaScript) {
            unsafe {
                +"""
      document.addEventListener("DOMContentLoaded", () => {
        const current_page = document.getElementById("current_page");
        const isMobile = window.matchMedia("(max-width: 767px)").matches;
        
        if(isMobile){
            current_page.style.boxSizing = "border-box";
            current_page.style.margin = "0px";
            current_page.style.padding = "15px";
            current_page.style.width = "99.25%";
        } else {            
            current_page.style.transform = "scale(1.2)";
            current_page.style.transformOrigin = "top left";
        }
        
      });
      """.trimIndent()
            }
        }
    }, style = {
        unsafe {
            +"""
                    ::selection {
                      background: ${Colors.primaryDark};
                      color: ${Colors.onPrimaryDark};
                    }
                    """.trimIndent()
        }
    }, fonts = listOf(
        "https://fonts.googleapis.com/icon?family=Material+Icons",
        "https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded",
        "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined",
        "https://fonts.googleapis.com/css2?family=Inter:wght@100;200;300;400;500;600;700;800;900&family=JetBrains+Mono:wght@100;200;300;400;500;600;700;800;900&display=swap"
    ), modifier = Modifier.padding(0.px).margin(0).backgroundColor(Colors.Background).custom(
        """
                          overflow-y: auto;
                    """.trimIndent()
    ), onTheHeadElement = {
        style {
            unsafe {
                raw(
                    """
                        @font-face {
                          font-family: 's_font_reg';
                          src: url('https://lucent-sunburst-156602.netlify.app/s_font-Regular.woff2') format('woff2');
                        }
                        @font-face {
                          font-family: 's_font_cur';
                          src: url('https://lucent-sunburst-156602.netlify.app/s_font-Cursive.ttf') format('truetype');
                        }
                """.trimIndent()
                )
            }
        }
        unsafe {

            +"""
                <link rel="icon" href="/images/kamp.png" type="image/png" />
            """.trimIndent()

            +"""
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                """.trimIndent()
        }
        unsafe {
            +"""
                <title>${metaTags.ogTitle.trim()}</title>
                  """.trimIndent()


            +"""
            <meta property="og:title" content="${metaTags.ogTitle.trim()}" />
            <meta property="og:type" content="${metaTags.pageType.type}" />
            <meta property="og:image" content="${(metaTags.deployTarget.baseUrl + metaTags.ogImageSrc).trim()}" />
            <meta property="og:description" content="${metaTags.ogDescription.trim()}" />
            <meta property="og:site_name" content="kamp" />
            
            <meta name="twitter:card" content="summary_large_image" />
            <meta name="twitter:title" content="${metaTags.ogTitle.trim()}" />
            <meta name="twitter:description" content="${metaTags.ogDescription.trim()}" />
            <meta name="twitter:image" content="${(metaTags.deployTarget.baseUrl + metaTags.ogImageSrc).trim()}" />
            <meta name="twitter:site" content="@sakethpathike" />
                """.trimIndent()
        }
    }) {
        content()
    }
}
