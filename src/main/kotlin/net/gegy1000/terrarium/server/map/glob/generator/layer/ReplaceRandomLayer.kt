package net.gegy1000.terrarium.server.map.glob.generator.layer

import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.IntCache

class ReplaceRandomLayer(val replace: Int, val replacement: Int, val chance: Int, seed: Long, parent: GenLayer) : GenLayer(seed) {
    init {
        this.parent = parent
    }

    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val parent = this.parent.getInts(areaX, areaY, areaWidth, areaHeight)
        val result = IntCache.getIntCache(areaWidth * areaHeight)
        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                val index = x + z * areaWidth
                val sample = parent[index]
                if (sample == this.replace && this.nextInt(this.chance) == 0) {
                    result[index] = this.replacement
                } else {
                    result[index] = sample
                }
            }
        }
        return result
    }
}
