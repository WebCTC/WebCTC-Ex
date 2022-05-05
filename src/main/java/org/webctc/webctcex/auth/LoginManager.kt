package org.webctc.webctcex.auth

import net.minecraft.entity.player.EntityPlayerMP
import java.security.SecureRandom
import java.util.*

class LoginManager {
    companion object {
        private val otpList = mutableMapOf<String, String>()
        private val keyList = mutableMapOf<String, String>()

        fun createOTP(player: EntityPlayerMP): String {
            val otp = getRandomPassword()
            otpList[player.commandSenderName] = otp
            return otp
        }

        fun getPlayer(id: String, otp: String): String? {
            val name = otpList
                .firstNotNullOfOrNull { if (it.key.equals(id, true) && it.value == otp) it.key else null }
            otpList.remove(name)
            return name
        }

        fun createRandomKey(name: String): String {
            val key = UUID.randomUUID().toString()
            keyList[key] = name
            return key
        }

        fun useKey(key: String): String? {
            return keyList.remove(key)
        }

        fun clear() = otpList.clear()

        private fun getRandomPassword(): String = String.format("%06d", SecureRandom().nextInt(999999))
    }
}