package org.webctc.webctcex.router

import cpw.mods.fml.common.FMLCommonHandler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import org.webctc.cache.Pos
import org.webctc.router.WebCTCRouter
import org.webctc.webctcex.WebCTCExCore
import org.webctc.webctcex.utils.RailGroup
import org.webctc.webctcex.utils.RailGroupManager
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class RailGroupRouter : WebCTCRouter() {
    companion object {
        val blockPosConnection = mutableMapOf<String, Connection?>()
        val signalPosConnection = mutableMapOf<String, Connection?>()
    }

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
                val type = call.request.queryParameters["type"]?.toIntOrNull()
                var railGroup: RailGroup? = null
                if (uuid != null && x != null && y != null && z != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    val pos = Pos(x, y, z)
                    when (type) {
                        0 -> railGroup?.addRail(pos)
                        1 -> railGroup?.addRS(pos)
                        2 -> railGroup?.addDisplayPos(pos)
                        else -> {}
                    }
                }
                val json = gson.toJson(railGroup?.toMutableMap())
                call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respondText(json)

                WebCTCExCore.INSTANCE.railGroupData.markDirty()
            }
            post("/addNextRailGroup") {
                val uuid = call.request.queryParameters["uuid"]?.let { UUID.fromString(it) }
                val nextRailGroup = call.request.queryParameters["nextRailGroup"]?.let { UUID.fromString(it) }
                var railGroup: RailGroup? = null
                if (uuid != null && nextRailGroup != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    railGroup?.addNextRailGroup(nextRailGroup)
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
                val type = call.request.queryParameters["type"]?.toIntOrNull()
                var railGroup: RailGroup? = null
                if (uuid != null && x != null && y != null && z != null) {
                    railGroup = RailGroupManager.railGroupList.find { it.uuid == uuid }
                    val pos = Pos(x, y, z)
                    when (type) {
                        0 -> railGroup?.removeRail(pos)
                        1 -> railGroup?.removeRS(pos)
                        2 -> railGroup?.removeDisplayPos(pos)
                        else -> {}
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
            webSocket("/BlockPosConnection") {
                val name = call.sessions.get<WebCTCExCore.UserSession>()?.name ?: return@webSocket
                this.initPosSetter("BlockPosSetter", name, blockPosConnection, Items.stick)
            }
            webSocket("/SignalPosConnection") {
                val name = call.sessions.get<WebCTCExCore.UserSession>()?.name ?: return@webSocket
                this.initPosSetter("SignalPosSetter", name, signalPosConnection, Items.blaze_rod)
            }
        }
    }
}

suspend fun DefaultWebSocketSession.initPosSetter(itemName: String, playerName: String, connectionList: MutableMap<String, Connection?>, item: Item) {
    val thisConnection = Connection(this)
    try {
        val itemStack = ItemStack(item).apply {
            this.tagCompound = NBTTagCompound().apply {
                this.setTag("ench", NBTTagList().apply {
                    this.appendTag(NBTTagCompound().apply {
                        this.setShort("id", 255)
                        this.setShort("lvl", 0)
                    })
                })
            }
            this.setStackDisplayName(itemName)
        }
        val player: EntityPlayer? = MinecraftServer.getServer().entityWorld.getPlayerEntityByName(playerName)
        if (player != null) {
            if (!player.inventory.mainInventory.all { ItemStack.areItemStacksEqual(it, itemStack) && ItemStack.areItemStackTagsEqual(it, itemStack) }) {
                player.inventory.addItemStackToInventory(itemStack)
            }
            player.entityWorld.playSoundAtEntity(player, "random.levelup", 1.0f, 1.0f)
            player.addChatComponentMessage(
                ChatComponentText(
                    "${EnumChatFormatting.GRAY}[${EnumChatFormatting.GREEN}Web${EnumChatFormatting.WHITE}CTC${EnumChatFormatting.GRAY}]" +
                            " " +
                            "${EnumChatFormatting.WHITE}Click block with ${EnumChatFormatting.GOLD}$itemName ${EnumChatFormatting.WHITE}to send pos to the web client."
                )
            )
            connectionList[playerName] = thisConnection

            for (frame in incoming) {
            }
        }
    } catch (e: Exception) {
        FMLCommonHandler.instance().fmlLogger.error(e.stackTrace.toString())
    }
    connectionList.remove(playerName, thisConnection)
}

class Connection(val session: DefaultWebSocketSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}