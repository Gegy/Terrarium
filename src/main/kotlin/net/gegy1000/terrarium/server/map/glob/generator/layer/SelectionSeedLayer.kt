package net.gegy1000.terrarium.server.map.glob.generator.layer

import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.IntCache

class SelectionSeedLayer(val range: Int, seed: Long) : GenLayer(seed) {
    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val result = IntCache.getIntCache(areaWidth * areaHeight)
        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                result[x + z * areaWidth] = this.nextInt(this.range)
            }
        }
        return result
    }
}
