package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.gen.feature.WorldGenTrees
import java.util.Random

class FreshFloodedForest : Forest(GlobType.FRESH_FLOODED_FOREST) {
    companion object {
        private val WATER: IBlockState = Blocks.WATER.defaultState
    }

    override fun decorate(random: Random, x: Int, z: Int) {
        this.decorateScatter(x, z, this.range(random, 5, 7), random) {
            var height = this.range(random, 3, 6)
            if (random.nextInt(10) == 0) {
                height += 3
            }
            if (random.nextBoolean()) {
                WorldGenTrees(false, height, OAK_LOG, OAK_LEAF, true).generate(this.world, random, it)
            } else {
                WorldGenTrees(false, height, BIRCH_LOG, BIRCH_LEAF, true).generate(this.world, random, it)
            }
        }
    }

    override fun getCover(x: Int, z: Int, random: Random) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector) {
            when (it) {
                Forest.LAYER_GRASS -> if (random.nextInt(3) != 0) FreshFloodedForest.WATER else Forest.PODZOL
                Forest.LAYER_DIRT -> Forest.PODZOL
                else -> Forest.COARSE_DIRT
            }
        }
    }

    override fun getFillerAt(x: Int, z: Int, random: Random) = COARSE_DIRT
}
