package sakethh.kamp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import sakethh.kamp.domain.model.Route
import sakethh.kamp.presentation.home.Home
import sakethh.kamp.presentation.utils.Colors
import sakethh.kapsule.Modifier
import sakethh.kapsule.Surface
import sakethh.kapsule.backgroundColor
import sakethh.kapsule.custom
import sakethh.kapsule.margin
import sakethh.kapsule.padding
import sakethh.kapsule.utils.px
import java.net.Inet4Address

fun main() {
    embeddedServer(Netty, port = 45454, host = Inet4Address.getLocalHost().hostName, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
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
                    Surface(fonts = listOf(
                        "https://fonts.googleapis.com/icon?family=Material+Icons",
                        "https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded",
                        "https://fonts.googleapis.com/css2?family=Inter&family=JetBrains+Mono&family=Megrim&display=swap"
                    ), modifier = Modifier.padding(0.px).margin(0).backgroundColor(Colors.Background).custom(
                        """
            overflow: hidden;
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
