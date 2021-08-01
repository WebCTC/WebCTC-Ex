package org.webctc.webctcex.utils

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.WorldSavedData

class RailGroupManager(mapName: String) : WorldSavedData(mapName) {
    override fun readFromNBT(nbt: NBTTagCompound) {
        railGroupList.clear()
        val tagList = nbt.getTagList("RailGroupData", 10)
        (0 until tagList.tagCount())
            .map { tagList.getCompoundTagAt(it) }
            .mapTo(railGroupList) { RailGroup.readFromNBT(it) }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = NBTTagList()
        railGroupList.map { it.writeToNBT() }.forEach { tagList.appendTag(it) }
        nbt.setTag("RailGroupData", tagList)
    }


    companion object {
        val railGroupList = mutableListOf<RailGroup>()
    }
}