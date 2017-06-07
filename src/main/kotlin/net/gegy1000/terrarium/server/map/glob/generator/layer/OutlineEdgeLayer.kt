package net.gegy1000.terrarium.server.map.glob.generator.layer

import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.IntCache

class OutlineEdgeLayer(val outline: Int, seed: Long, parent: GenLayer) : GenLayer(seed) {
    init {
        this.parent = parent
    }

    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val sampleX = areaX - 1
        val sampleZ = areaY - 1
        val sampleWidth = areaWidth + 2
        val sampleHeight = areaHeight + 2
        val parent = this.parent.getInts(sampleX, sampleZ, sampleWidth, sampleHeight)

        val result = IntCache.getIntCache(areaWidth * areaHeight)

        var last: Int
        for (z in 0..areaHeight - 1) {
            last = -1
            for (x in 0..areaWidth - 1) {
                val sample = parent[x + 1 + (z + 1) * sampleWidth]
                if (last != sample && last != -1) {
                    result[x + z * areaWidth] = this.outline
                } else {
                    result[x + z * areaWidth] = sample
                }
                last = sample
            }
        }

        for (x in 0..areaWidth - 1) {
            last = -1
            for (z in 0..areaWidth - 1) {
                val sample = parent[x + 1 + (z + 1) * sampleWidth]
                val resultSample = result[x + z * areaWidth]
                if (resultSample != this.outline) {
                    if (last != sample && last != -1) {
                        result[x + z * areaWidth] = this.outline
                    } else {
                        result[x + z * areaWidth] = sample
                    }
                }
                last = sample
            }
        }

        return result
    }
}
