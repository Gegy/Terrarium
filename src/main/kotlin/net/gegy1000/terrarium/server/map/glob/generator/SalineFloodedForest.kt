package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.gen.feature.WorldGenTrees
import java.util.Random

class SalineFloodedForest : Forest(GlobType.SALINE_FLOODED_FOREST) {
    companion object {
        private val WATER: IBlockState = Blocks.WATER.defaultState
    }

    override fun decorate(world: World, random: Random, x: Int, z: Int) {
        this.decorateScatter(x, z, this.range(random, 4, 8), world, random) {
            if (random.nextInt(5) == 0) {
                var height = this.range(random, 3, 6)
                if (random.nextInt(10) == 0) {
                    height += 3
                }
                WorldGenTrees(false, height, Forest.OAK_LOG, Forest.OAK_LEAF, true).generate(world, random, it)
            } else {
                val height = this.range(random, 6, 12)
                WorldGenTrees(false, height, Forest.JUNGLE_LOG, Forest.JUNGLE_LEAF, true).generate(world, random, it)
            }
        }
    }

    override fun getCover(glob: Array<GlobType>, cover: Array<IBlockState>, x: Int, z: Int, random: Random) {
        val coverLayer = this.sampleChunk(this.coverSelector, x, z)

        this.foreach(glob) { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            cover[index] = when (coverLayer[index]) {
                Forest.LAYER_GRASS -> if (random.nextInt(5) != 0) SalineFloodedForest.WATER else Forest.PODZOL
                Forest.LAYER_DIRT -> Forest.PODZOL
                else -> Forest.COARSE_DIRT
            }
        }
    }

    override fun getFiller(x: Int, z: Int, random: Random) = Forest.COARSE_DIRT
}
