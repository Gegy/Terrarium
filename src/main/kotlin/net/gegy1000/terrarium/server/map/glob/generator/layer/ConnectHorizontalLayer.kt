package net.gegy1000.terrarium.server.map.glob.generator.layer

import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.IntCache

class ConnectHorizontalLayer(val connect: Int, seed: Long, parent: GenLayer) : GenLayer(seed) {
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

        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                val parentX = x + 1
                val parentZ = z + 1
                val sample = parent[parentX + parentZ * sampleWidth]
                var type = sample
                if (sample != this.connect) {
                    val east = parent[(parentX + 1) + parentZ * sampleWidth] == this.connect
                    val west = parent[(parentX - 1) + parentZ * sampleWidth] == this.connect
                    val south = parent[parentX + (parentZ + 1) * sampleWidth] == this.connect
                    if (east && south) {
                        if (parent[(parentX + 1) + (parentZ + 1) * sampleWidth] != this.connect) {
                            type = this.connect
                        }
                    } else if (west && south) {
                        if (parent[(parentX - 1) + (parentZ + 1) * sampleWidth] != this.connect) {
                            type = this.connect
                        }
                    }
                }
                result[x + z * areaWidth] = type
            }
        }

        return result
    }
}
