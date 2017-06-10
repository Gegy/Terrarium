package net.gegy1000.terrarium.server.map.glob.generator.layer

import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.IntCache

class SelectWeightedLayer(seed: Long, vararg val entries: Entry) : GenLayer(seed) {
    val totalWeight: Int

    init {
        this.totalWeight = this.entries.sumBy { it.weight }
    }

    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val result = IntCache.getIntCache(areaWidth * areaHeight)
        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                val selectedWeight = this.nextInt(this.totalWeight)
                var currentWeight = 0
                for ((id, weight) in this.entries) {
                    currentWeight += weight
                    if (currentWeight >= selectedWeight) {
                        result[x + z * areaWidth] = id
                        break
                    }
                }
            }
        }
        return result
    }

    data class Entry(val id: Int, val weight: Int)
}
