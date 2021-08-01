package org.webctc.webctcex.router

import express.http.SessionCookie
import express.http.response.Response
import express.middleware.Middleware
import express.utils.MediaType
import express.utils.Status
import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.util.ResourceLocation
import org.webctc.WebCTCCore
import org.webctc.router.WebCTCRouter
import org.webctc.webctcex.auth.LoginManager


class ExRouter : WebCTCRouter() {
    init {
        use(Middleware.cookieSession("jwyyLID3sG", 9000))

        post("/login") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data == null) {
                val id = req.getFormQuery("id")
                val password = req.getFormQuery("password")
                val player = LoginManager.getPlayer(id, password)
                if (player == null) {
                    res.send("Login failed!")
                } else {
                    sessionCookie.data = player
                    res.redirect("railgroup")
                }
            } else {
                res.redirect("railgroup")
            }
        }

        all("/logout") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                sessionCookie.data = null
            }
            res.redirect("../login.html")
        }

        get("/railgroup") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                this.sendFile("railgroup.html", res, MediaType._html)
            } else {
                res.setStatus(Status._401)
                res.redirect("../login.html")
            }
        }

        get("/railgroup.js") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                this.sendFile("railgroup.js", res, MediaType._css)
            } else {
                res.setStatus(Status._401)
                res.redirect("../login.html")
            }
        }

        get("/stylesheet.css") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                this.sendFile("stylesheet.css", res, MediaType._css)
            } else {
                res.setStatus(Status._401)
                res.redirect("../login.html")
            }
        }
    }

    private fun sendFile(fileName: String, res: Response, mediaType: MediaType) {
        try {
            val inputStream = NGTFileLoader.getInputStream(ResourceLocation(WebCTCCore.MODID, "html/ex/$fileName"))
            res.streamFrom(0, inputStream, mediaType)
        } catch (e: Exception) {
            res.sendStatus(Status._401)
        }
    }
}