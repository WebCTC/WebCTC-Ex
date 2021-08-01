package org.webctc.webctcex.router

import express.http.SessionCookie
import express.utils.MediaType
import express.utils.Status
import org.webctc.cache.Pos
import org.webctc.router.WebCTCRouter
import org.webctc.webctcex.WebCTCExCore
import org.webctc.webctcex.utils.RailGroup
import org.webctc.webctcex.utils.RailGroupManager
import java.net.URLDecoder
import java.util.*

class RailGroupRouter : WebCTCRouter() {
    init {

        get("/") { req, res ->
            res.setContentType(MediaType._json)
            res.setHeader("Access-Control-Allow-Origin", "*")
            val json = gson.toJson(RailGroupManager.railGroupList)
            res.send(json)
        }

        get("/railgroup") { req, res ->
            res.setContentType(MediaType._json)
            res.setHeader("Access-Control-Allow-Origin", "*")
            val uuid = req.getQuery("uuid")?.let { UUID.fromString(it) }
            val railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
            val json = gson.toJson(railGroup?.toMutableMap())
            res.send(json)
        }

        post("/create") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                res.setContentType(MediaType._json)
                res.setHeader("Access-Control-Allow-Origin", "*")
                val railGroup = RailGroup.create()
                val json = gson.toJson(railGroup.toMutableMap())
                res.send(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            } else {
                res.sendStatus(Status._401)
            }
        }

        post("/delete") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                res.setContentType(MediaType._json)
                res.setHeader("Access-Control-Allow-Origin", "*")
                val uuid = req.getQuery("uuid")?.let { UUID.fromString(it) }
                val removed = uuid?.let { RailGroup.delete(it) }
                val json = gson.toJson(mutableMapOf("removed" to removed))
                res.send(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            } else {
                res.sendStatus(Status._401)
            }
        }

        post("/update") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                res.setContentType(MediaType._json)
                res.setHeader("Access-Control-Allow-Origin", "*")
                val uuid = req.getQuery("uuid")?.let { UUID.fromString(it) }
                val name = req.getQuery("name")
                var railGroup: RailGroup? = null
                if (uuid != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    if (name != null) {
                        railGroup?.setName(URLDecoder.decode(name, "UTF-8"))
                    }
                }
                val json = gson.toJson(railGroup?.toMutableMap())
                res.send(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            } else {
                res.sendStatus(Status._401)
            }
        }

        post("/add") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                res.setContentType(MediaType._json)
                res.setHeader("Access-Control-Allow-Origin", "*")
                val uuid = req.getQuery("uuid")?.let { UUID.fromString(it) }
                val x = req.getQuery("x")?.toIntOrNull()
                val y = req.getQuery("y")?.toIntOrNull()
                val z = req.getQuery("z")?.toIntOrNull()
                val rs = req.getQuery("rs").toBoolean()
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
                res.send(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            } else {
                res.sendStatus(Status._401)
            }
        }

        post("/remove") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                res.setContentType(MediaType._json)
                res.setHeader("Access-Control-Allow-Origin", "*")
                val uuid = req.getQuery("uuid")?.let { UUID.fromString(it) }
                val x = req.getQuery("x")?.toIntOrNull()
                val y = req.getQuery("y")?.toIntOrNull()
                val z = req.getQuery("z")?.toIntOrNull()
                val rs = req.getQuery("rs").toBoolean()
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
                res.send(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            } else {
                res.sendStatus(Status._401)
            }
        }

        post("/clear") { req, res ->
            val sessionCookie = req.getMiddlewareContent("sessioncookie") as SessionCookie
            if (sessionCookie.data != null) {
                res.setContentType(MediaType._json)
                res.setHeader("Access-Control-Allow-Origin", "*")
                val uuid = req.getQuery("uuid")?.let { UUID.fromString(it) }
                val rs = req.getQuery("rs").toBoolean()
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
                res.send(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            } else {
                res.sendStatus(Status._401)
            }
        }
    }
}