package org.webctc.webctcex.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.util.ResourceLocation
import org.webctc.WebCTCCore
import org.webctc.router.WebCTCRouter
import org.webctc.webctcex.WebCTCExCore
import org.webctc.webctcex.auth.LoginManager


class ExRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get("/login") {
            call.request.queryParameters["key"]?.let {
                LoginManager.useKey(it)?.let { name ->
                    call.sessions.set(WebCTCExCore.UserSession(name))
                    call.respondRedirect("/ex/railgroup")
                    return@get
                }
            }
            call.respondText("Login failed", ContentType.Text.Plain)
        }

        authenticate("auth-session") {
            get("/logout") {
                call.sessions.clear<WebCTCExCore.UserSession>()
                call.respondRedirect("/ex/login")
            }
            get("/railgroup") {
                call.respondResourceLocatedFile("railgroup.html", ContentType.Text.Html)
            }
            get("/railgroup.js") {
                call.respondResourceLocatedFile("railgroup.js", ContentType.Text.JavaScript)
            }
            get("/stylesheet.css") {
                call.respondResourceLocatedFile("stylesheet.css", ContentType.Text.CSS)
            }
        }
    }

    private suspend fun ApplicationCall.respondResourceLocatedFile(fileName: String, contentType: ContentType) {
        val inputStream = NGTFileLoader.getInputStream(ResourceLocation(WebCTCCore.MODID, "html/ex/$fileName"))
        this.respondText(inputStream.bufferedReader().readText(), contentType)
    }
}