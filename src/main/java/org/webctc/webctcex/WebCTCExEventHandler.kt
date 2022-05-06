package org.webctc.webctcex

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import io.ktor.websocket.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.WorldServer
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import org.webctc.cache.Pos
import org.webctc.router.WebCTCRouter
import org.webctc.webctcex.router.Connection
import org.webctc.webctcex.router.RailGroupRouter
import java.util.*
import kotlin.concurrent.thread

class WebCTCExEventHandler {
    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.entityPlayer
        if (event.world is WorldServer &&
            event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK &&
            player.heldItem?.item == Items.stick &&
            player.heldItem?.isItemEnchanted == true
        ) {
            thread {
                val pos = Pos(event.x, event.y, event.z)
                if (player.heldItem?.displayName == "BlockPosSetter") {
                    RailGroupRouter.blockPosConnection[player.commandSenderName]?.trySendBlockPos(player, pos)
                }
            }
        }
    }
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
