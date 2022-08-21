package org.webctc.webctcex.utils

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import org.webctc.cache.Pos
import org.webctc.cache.rail.RailCacheData
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class RailGroup {
    private var name = "Default Name"
    val uuid: UUID
    val railPosList = CopyOnWriteArraySet<Pos>()
    val rsPosList = CopyOnWriteArraySet<Pos>()

    private constructor() {
        this.uuid = UUID.randomUUID()
    }

    private constructor(uuid: UUID) {
        this.uuid = uuid
    }

    fun addRail(pos: Pos) {
        this.railPosList.add(pos)
    }

    fun removeRail(pos: Pos) {
        this.railPosList.remove(pos)
    }

    fun clearRail() {
        this.railPosList.clear()
    }

    fun addRS(pos: Pos) {
        this.rsPosList.add(pos)
    }

    fun removeRS(pos: Pos) {
        this.rsPosList.remove(pos)
    }

    fun clearRS() {
        this.rsPosList.clear()
    }

    fun setName(name: String) {
        this.name = name
    }

    fun isTrainOnRail(): Boolean {
        return railPosList
            .mapNotNull { RailCacheData.railMapCache[it] }
            .any { it.isTrainOnRail }
    }

    fun writeToNBT(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setString("name", this.name)
        tag.setString("uuid", this.uuid.toString())

        val railPosTagList = NBTTagList()
        railPosList.forEach { railPosTagList.appendTag(it.writeToNBT()) }
        tag.setTag("railPosTagList", railPosTagList)
        val rsPosTagList = NBTTagList()
        rsPosList.forEach { rsPosTagList.appendTag(it.writeToNBT()) }
        tag.setTag("rsPosTagList", rsPosTagList)
        return tag
    }

    fun toMutableMap(): Map<String, Any?> {
        return mutableMapOf(
            "uuid" to this.uuid,
            "railPosList" to this.railPosList,
            "rsPosList" to this.rsPosList,
            "name" to this.name
        )
    }

    companion object {

        fun create(): RailGroup {
            val railGroup = RailGroup()
            RailGroupManager.railGroupList.add(railGroup)
            return railGroup
        }

        fun delete(uuid: UUID): Boolean {
            return RailGroupManager.railGroupList.removeIf { it.uuid == uuid }
        }

        fun readFromNBT(nbt: NBTTagCompound): RailGroup {
            val uuid = UUID.fromString(nbt.getString("uuid"))
            val railGroup = RailGroup(uuid)
            val name = nbt.getString("name")
            railGroup.setName(name)
            val railPosTagList = nbt.getTagList("railPosTagList", 10)
            (0 until railPosTagList.tagCount())
                .map { railPosTagList.getCompoundTagAt(it) }
                .map { Pos.readFromNBT(it) }
                .forEach { railGroup.addRail(it) }
            val rsPosTagList = nbt.getTagList("rsPosTagList", 10)
            (0 until rsPosTagList.tagCount())
                .map { rsPosTagList.getCompoundTagAt(it) }
                .map { Pos.readFromNBT(it) }
                .forEach { railGroup.addRS(it) }

            return railGroup
        }
    }
}