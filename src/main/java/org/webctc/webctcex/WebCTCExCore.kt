package org.webctc.webctcex

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import net.minecraft.init.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import org.webctc.router.RouterManager
import org.webctc.webctcex.auth.LoginManager
import org.webctc.webctcex.command.CommandRailGroup
import org.webctc.webctcex.command.CommandWebCTCEx
import org.webctc.webctcex.router.ExRouter
import org.webctc.webctcex.router.RailGroupRouter
import org.webctc.webctcex.utils.RailGroupManager


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
    }


    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
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
            val world = MinecraftServer.getServer().entityWorld;
            RailGroupManager.railGroupList.forEach { it ->
                val isTrainOnRail = it.isTrainOnRail()
                val block = if (isTrainOnRail) Blocks.redstone_block else Blocks.stained_glass
                it.rsPosList.forEach { world.setBlock(it.x, it.y, it.z, block, 14, 3) }
            }
        }
    }

    companion object {
        const val MODID = "webctc_ex"
        const val VERSION = "0.4.0"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCExCore
    }
}