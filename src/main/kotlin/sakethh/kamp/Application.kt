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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.html.stream.createHTML
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

val blogFileNames = listOf("synchronization-in-linkora")

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
        Route(route = "/", content = {
            with(it) {
                this.Home()
            }
        }),
        Route(route = "/blog", content = {
            with(it) {
                this.BlogList()
            }
        }),
    )
    blogFileNames.forEach { fileName ->
        allRoutes.add(
            Route(
                route = "/blog/${fileName}", content = {
                    with(it) {
                        BlogPage(fileName)
                    }
                })
        )
    }
    routing {
        authenticate(Constants.BEARER_AUTH) {
            post(path = "/snapshot/push") {
                call.respond(withContext(Dispatchers.IO) {
                    SnapshotManager.pushANewSnapshot().fold(onSuccess = { it }, onFailure = { it.stackTraceToString() })
                })
            }
        }
        staticResources(remotePath = "/", basePackage = "static")
        allRoutes.forEach { currentRoute ->
            get(currentRoute.route) {
                call.respondText(contentType = ContentType.Text.Html, text = createHTML().html {
                    KampSurface {
                        currentRoute.content(this)
                    }
                })
            }
        }
    }
}

fun HTML.KampSurface(content: BODY.() -> Unit) {
    Surface(
        onTheBodyElement = {
        script(type = ScriptType.textJavaScript) {
            unsafe {
                +"""
      document.addEventListener("DOMContentLoaded", () => {
        const current_page = document.getElementById("current_page");
        const footerOnBlogLists = document.getElementById("footerOnBlogLists");
        const isMobile = window.matchMedia("(max-width: 767px)").matches;
        
        if(isMobile){
            current_page.style.boxSizing = "border-box";
            current_page.style.padding = "15px";
            current_page.style.width = "99.25%";
            footerOnBlogLists.style.padding = "15px";
            footerOnBlogLists.style.width = "99.25%";
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
        unsafe {
            raw(
                """
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                """.trimIndent()
            )
        }
    }) {
        content()
    }
}
