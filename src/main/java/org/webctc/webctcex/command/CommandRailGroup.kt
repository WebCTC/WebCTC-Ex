package org.webctc.webctcex.command

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import org.webctc.webctcex.utils.RailGroupManager
import java.util.*

class CommandRailGroup : CommandBase() {
    override fun getCommandName() = "railgroup"

    override fun getCommandUsage(sender: ICommandSender) = "/railgroup setsignal {UUID} {SignalLevel}"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.size > 1) {
            when (args[0].lowercase()) {
                "setsignal" -> {
                    if (args.size == 3) {
                        val uuid = UUID.fromString(args[1])
                        val signal = args[2].toInt()
                        RailGroupManager.setSignal(uuid, signal)
                        sender.addChatMessage(ChatComponentText("[RailGroup] Set signal: $signal to uuid: $uuid"))
                    }
                }
            }
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>): List<String>? {
        return if (args.size == 1) listOf("setsignal") else null
    }
}