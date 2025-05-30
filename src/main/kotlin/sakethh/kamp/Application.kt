package sakethh.kamp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.ScriptType
import kotlinx.html.html
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import sakethh.kamp.domain.model.Route
import sakethh.kamp.presentation.home.Home
import sakethh.kamp.presentation.utils.Colors
import sakethh.kapsule.*
import sakethh.kapsule.utils.px

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS){
        allowHost(host = "sakethpathike.github.io")
        allowHost(host = "sakethpathike.netlify.app")
    }
    val allRoutes = listOf(Route(route = "/", content = {
        with(it) {
            this.Home()
        }
    }))
    routing {
        staticResources(remotePath = "/", basePackage = "static")
        allRoutes.forEach { currentRoute ->
            get(currentRoute.route) {
                call.respondText(contentType = ContentType.Text.Html, text = createHTML().html {
                    Surface(
                        onTheBodyElement = {
                        script(type = ScriptType.textJavaScript) {
                            unsafe {
                                +"""
      document.addEventListener("DOMContentLoaded", () => {
        const home_page = document.getElementById("home_page");
        const isMobile = window.matchMedia("(max-width: 767px)").matches;
        
        if(isMobile){
            home_page.style.boxSizing = "border-box";
            home_page.style.padding = "15px";
            home_page.style.width = "100%";
        } else {            
            home_page.style.transform = "scale(1.2)";
            home_page.style.transformOrigin = "top left";
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
                        "https://fonts.googleapis.com/css2?family=Inter:wght@400;700;900&family=JetBrains+Mono:wght@400;700&display=swap"
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
                        currentRoute.content(this)
                    }
                })
            }
        }
    }
}
