package org.webctc.webctcex.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.webctc.cache.Pos
import org.webctc.router.WebCTCRouter
import org.webctc.webctcex.WebCTCExCore
import org.webctc.webctcex.utils.RailGroup
import org.webctc.webctcex.utils.RailGroupManager
import java.net.URLDecoder
import java.util.*

class RailGroupRouter : WebCTCRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            val json = gson.toJson(RailGroupManager.railGroupList)
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            call.respondText(json)
        }
        get("/railgroup") {
            val uuid = call.request.queryParameters["uuid"]?.let { UUID.fromString(it) }
            val railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
            val json = gson.toJson(railGroup?.toMutableMap())
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            call.respondText(json)
        }

        authenticate("auth-session") {
            post("/create") {
                val railGroup = RailGroup.create()
                val json = gson.toJson(railGroup.toMutableMap())
                call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respondText(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            }
            post("/delete") {
                val uuid = call.request.queryParameters["uuid"]?.let { UUID.fromString(it) }
                val removed = uuid?.let { RailGroup.delete(it) }
                val json = gson.toJson(mutableMapOf("removed" to removed))
                call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respondText(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            }
            post("/update") {
                val uuid = call.request.queryParameters["uuid"]?.let { UUID.fromString(it) }
                val name = call.request.queryParameters["name"]
                var railGroup: RailGroup? = null
                if (uuid != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    if (name != null) {
                        railGroup?.setName(withContext(Dispatchers.IO) {
                            URLDecoder.decode(name, "UTF-8")
                        })
                    }
                }
                val json = gson.toJson(railGroup?.toMutableMap())
                call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respondText(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            }
            post("/add") {
                val uuid = call.request.queryParameters["uuid"]?.let { UUID.fromString(it) }
                val x = call.request.queryParameters["x"]?.toIntOrNull()
                val y = call.request.queryParameters["y"]?.toIntOrNull()
                val z = call.request.queryParameters["z"]?.toIntOrNull()
                val rs = call.request.queryParameters["rs"].toBoolean()
                var railGroup: RailGroup? = null
                if (uuid != null && x != null && y != null && z != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    val pos = Pos(x, y, z)
                    if (rs) {
                        railGroup?.addRS(pos)
                    } else {
                        railGroup?.addRail(pos)
                    }
                }
                val json = gson.toJson(railGroup?.toMutableMap())
                call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respondText(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            }
            post("/remove") {
                val uuid = call.request.queryParameters["uuid"]?.let { UUID.fromString(it) }
                val x = call.request.queryParameters["x"]?.toIntOrNull()
                val y = call.request.queryParameters["y"]?.toIntOrNull()
                val z = call.request.queryParameters["z"]?.toIntOrNull()
                val rs = call.request.queryParameters["rs"].toBoolean()
                var railGroup: RailGroup? = null
                if (uuid != null && x != null && y != null && z != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    val pos = Pos(x, y, z)
                    if (rs) {
                        railGroup?.removeRS(pos)
                    } else {
                        railGroup?.removeRail(pos)
                    }
                }
                val json = gson.toJson(railGroup?.toMutableMap())
                call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respondText(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            }
            post("/clear") {
                val uuid = call.request.queryParameters["uuid"]?.let { UUID.fromString(it) }
                val rs = call.request.queryParameters["rs"].toBoolean()
                var railGroup: RailGroup? = null
                if (uuid != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    if (rs) {
                        railGroup?.clearRS()
                    } else {
                        railGroup?.clearRail()
                    }
                }
                val json = gson.toJson(railGroup?.toMutableMap())
                call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respondText(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            }
        }
    }
}