package org.webctc.webctcex.command

import jp.ngt.ngtlib.util.NGTUtil
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import org.webctc.WebCTCConfig
import org.webctc.webctcex.auth.LoginManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class CommandWebCTCEx : CommandBase() {
    private val ip: String

    init {
        val url = URL("http://checkip.amazonaws.com")
        ip = BufferedReader(InputStreamReader(url.openStream())).use { it.readLine() }
    }


    override fun getCommandName() = "webctcex"

    override fun getCommandUsage(sender: ICommandSender) = "/webctcex access"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (sender is EntityPlayerMP) {
            sender.addChatMessage(ChatComponentText(("[WebCTCEx] Access granted!")))
            val text = ChatComponentText("URL: ")
            val url = ChatComponentText(WebCTCConfig.getURL() + ":" + WebCTCConfig.getPort() + "/login.html")
            url.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url.chatComponentText_TextValue)
            text.appendSibling(url)
            sender.addChatMessage(text)

            val password = LoginManager.createOTP(sender)
            sender.addChatMessage(ChatComponentText(("OTP: $password")))
        } else {
            sender.addChatMessage(ChatComponentText(("[WebCTCEx] Sorry! This command can only be executed by player.")))
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>): List<String>? {
        return if (args.size == 1) listOf("access") else null
    }

    private fun WebCTCConfig.Companion.getPort(): Int {
        return when (accessPort) {
            0 -> portNumber
            else -> accessPort
        }
    }

    private fun WebCTCConfig.Companion.getURL(): String {
        return when (accessUrl) {
            "" -> "http://" + if (NGTUtil.isSMP()) ip else "localhost"
            else -> accessUrl
        }
    }
}
