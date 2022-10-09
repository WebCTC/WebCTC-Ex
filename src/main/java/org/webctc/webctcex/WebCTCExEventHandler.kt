package org.webctc.webctcex

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import io.ktor.websocket.*
import jp.ngt.rtm.RTMBlock
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.WorldServer
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.BlockEvent
import org.webctc.cache.Pos
import org.webctc.router.WebCTCRouter
import org.webctc.webctcex.router.Connection
import org.webctc.webctcex.router.RailGroupRouter
import org.webctc.webctcex.utils.RailGroupManager
import java.util.*
import kotlin.concurrent.thread

class WebCTCExEventHandler {
    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.entityPlayer
        val itemStack = player.heldItem
        if (event.world is WorldServer &&
            event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK &&
            itemStack?.isItemEnchanted == true
        ) {
            val item = itemStack.item
            thread {
                val pos = Pos(event.x, event.y, event.z)
                if (item == Items.stick && itemStack.displayName == "BlockPosSetter") {
                    RailGroupRouter.blockPosConnection[player.commandSenderName]?.trySendBlockPos(player, pos)
                } else if (item == Items.blaze_rod && itemStack.displayName == "SignalPosSetter" && event.targetBlock() == RTMBlock.signal) {
                    RailGroupRouter.signalPosConnection[player.commandSenderName]?.trySendBlockPos(player, pos)
                }
            }
        }
    }

    @SubscribeEvent
    fun onBreakBlock(event: BlockEvent.BreakEvent) {
        val tile = event.world.getTileEntity(event.x, event.y, event.z)
        if (tile is TileEntityLargeRailBase) {
            val core = tile.railCore
            val pos = Pos(core.xCoord, core.yCoord, core.zCoord)
            val usedByWebCTC = RailGroupManager.railGroupList.map { it.railPosList }.flatten().any { it == pos }
            if (usedByWebCTC) {
                event.player.addChatComponentMessage(
                    ChatComponentText(
                        "${EnumChatFormatting.RED}This rail is managed by WebCTC(RailGroup)."
                    )
                )
                event.player.addChatComponentMessage(
                    ChatComponentText(
                        "${EnumChatFormatting.RED}If you want to break this rail, first remove it from ${RailGroupManager.railGroupList.filter { it.railPosList.contains(pos) }.map { it.getName() }}."
                    )
                )
                event.isCanceled = true
            }
        }
    }
}

fun PlayerInteractEvent.targetBlock(): Block {
    return this.world.getBlock(this.x, this.y, this.z)
}

fun Connection.trySendBlockPos(player: EntityPlayer, pos: Pos) {
    val frame = Frame.Text(WebCTCRouter.gson.toJson(pos))
    if (this.session.outgoing.trySend(frame).isSuccess) {
        player.addChatComponentMessage(
            ChatComponentText(
                "${EnumChatFormatting.GRAY}[${EnumChatFormatting.GREEN}Web${EnumChatFormatting.WHITE}CTC${EnumChatFormatting.GRAY}]" +
                        " " +
                        "${EnumChatFormatting.WHITE}$pos was sent to web client."
            )
        )
        player.playSoundAtEntity("random.bow", 0.5f, 0.4f / (Random().nextFloat() * 0.4f + 0.8f))
    }
}

private fun EntityPlayer.playSoundAtEntity(name: String, volute: Float, pitch: Float) {
    this.worldObj.playSoundAtEntity(this, name, volute, pitch)
}
