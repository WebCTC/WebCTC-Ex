package org.webctc.webctcex

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.MinecraftForge
import org.webctc.plugin.PluginManager
import org.webctc.router.RouterManager
import org.webctc.webctcex.auth.LoginManager
import org.webctc.webctcex.command.CommandRailGroup
import org.webctc.webctcex.command.CommandWebCTCEx
import org.webctc.webctcex.router.ExRouter
import org.webctc.webctcex.router.RailGroupRouter
import org.webctc.webctcex.utils.RailGroupManager
import java.security.SecureRandom


@Mod(
    modid = WebCTCExCore.MODID,
    version = WebCTCExCore.VERSION,
    name = WebCTCExCore.MODID,
    acceptableRemoteVersions = "*"
)
class WebCTCExCore {
    lateinit var railGroupData: WorldSavedData

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        FMLCommonHandler.instance().bus().register(this)
        FMLCommonHandler.instance().bus().register(WebCTCExEventHandler())
        MinecraftForge.EVENT_BUS.register(WebCTCExEventHandler())
    }

    data class UserSession(val name: String) : Principal

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        val secretEncryptKey = SecureRandom.getInstanceStrong().generateSeed(16)
        val secretSignKey = SecureRandom.getInstanceStrong().generateSeed(16)
        PluginManager.registerPlugin {
            install(Sessions) {
                cookie<UserSession>("ex-session", SessionStorageMemory()) {
                    cookie.path = "/"
                    cookie.maxAgeInSeconds = 60 * 60 * 6
                    transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
                }
            }
            install(Authentication) {
                session<UserSession>("auth-session") {
                    validate { session ->
                        if (session.name.isNotEmpty()) session else null
                    }
                    challenge {
                        call.respondRedirect("/ex/login")
                    }
                }
            }
        }

        RouterManager.registerRouter("/api/railgroups", RailGroupRouter())
        RouterManager.registerRouter("/ex", ExRouter())
    }

    @Mod.EventHandler
    fun handleServerStarting(event: FMLServerStartingEvent) {
        val world = event.server.entityWorld
        var railGroupData = world.mapStorage.loadData(RailGroupManager::class.java, "webctcex_railgroup")

        if (railGroupData == null) {
            railGroupData = RailGroupManager("webctcex_railgroup")
            world.mapStorage.setData("webctcex_railgroup", railGroupData)
        }
        this.railGroupData = railGroupData

        event.registerServerCommand(CommandWebCTCEx())
        event.registerServerCommand(CommandRailGroup())
        LoginManager.clear()
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            RailGroupManager.railGroupList.forEach { it.update() }
        }
    }

    companion object {
        const val MODID = "webctc_ex"
        const val VERSION = "0.5.0"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCExCore
    }
}