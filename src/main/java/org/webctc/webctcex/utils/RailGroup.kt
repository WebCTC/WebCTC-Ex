package org.webctc.webctcex.utils

import jp.ngt.rtm.electric.TileEntitySignal
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.server.MinecraftServer
import org.webctc.cache.Pos
import org.webctc.cache.rail.RailCacheData
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class RailGroup {
    private var name = "Default Name"
    val uuid: UUID
    val railPosList = CopyOnWriteArraySet<Pos>()
    val rsPosList = CopyOnWriteArraySet<Pos>()
    val nextRailGroupList = CopyOnWriteArraySet<UUID>()
    val displayPosList = CopyOnWriteArraySet<Pos>()

    var signalLevel = 0

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

    fun getName(): String {
        return this.name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun addNextRailGroup(uuid: UUID) {
        this.nextRailGroupList.add(uuid)
    }

    fun removeNextRailGroup(uuid: UUID) {
        this.nextRailGroupList.remove(uuid)
    }

    fun addDisplayPos(pos: Pos) {
        this.displayPosList.add(pos)
    }

    fun removeDisplayPos(pos: Pos) {
        this.displayPosList.remove(pos)
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
        val nextRailGroupTagList = NBTTagList()
        nextRailGroupList.forEach { nextRailGroupTagList.appendTag(it.writeToNBT()) }
        tag.setTag("nextRailGroupTagList", nextRailGroupTagList)
        val displayPosTagList = NBTTagList()
        displayPosList.forEach { displayPosTagList.appendTag(it.writeToNBT()) }
        tag.setTag("displayPosTagList", displayPosTagList)
        return tag
    }

    fun toMutableMap(): Map<String, Any?> {
        return mutableMapOf(
            "uuid" to this.uuid,
            "railPosList" to this.railPosList,
            "rsPosList" to this.rsPosList,
            "name" to this.name,
            "nextRailGroupList" to this.nextRailGroupList,
            "displayPosList" to this.displayPosList
        )
    }

    fun UUID.writeToNBT(): NBTTagString {
        return NBTTagString(this.toString())
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
            val nextRailGroupTagList = nbt.getTagList("nextRailGroupTagList", 8)
            (0 until nextRailGroupTagList.tagCount())
                .map { nextRailGroupTagList.getStringTagAt(it) }
                .map { UUID.fromString(it) }
                .forEach { railGroup.addNextRailGroup(it) }
            val displayPosTagList = nbt.getTagList("displayPosTagList", 10)
            (0 until displayPosTagList.tagCount())
                .map { displayPosTagList.getCompoundTagAt(it) }
                .map { Pos.readFromNBT(it) }
                .forEach { railGroup.addDisplayPos(it) }

            return railGroup
        }
    }

    fun update() {
        val isTrainOnRail = this.isTrainOnRail()
        this.signalLevel = ((if (isTrainOnRail) 0
        else this.nextRailGroupList.mapNotNull { uuid ->
            RailGroupManager.railGroupList.find { it.uuid == uuid }
        }.minOfOrNull {
            it.signalLevel
        } ?: 0) + 1).coerceAtMost(6)
        val world = MinecraftServer.getServer().entityWorld
        this.displayPosList
            .map { world.getTileEntity(it.x, it.y, it.z) }
            .filterIsInstance(TileEntitySignal::class.java)
            .filter { it.javaClass.fields.find { it.name == "signalLevel" }?.apply { this.isAccessible = true }?.get(it) != this.signalLevel }
            .forEach { it.setElectricity(it.xCoord, it.yCoord, it.zCoord, this.signalLevel) }

        val block = if (isTrainOnRail) Blocks.redstone_block else Blocks.stained_glass
        this.rsPosList.forEach { world.setBlock(it.x, it.y, it.z, block, 14, 3) }
    }
}