package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.world.World
import net.minecraft.world.gen.feature.WorldGenTrees
import java.util.Random

class MixedBroadNeedleleaf : Forest(GlobType.MIXED_BROAD_NEEDLELEAF) {
    override fun decorate(world: World, random: Random, x: Int, z: Int) {
        this.decorateScatter(x, z, this.range(random, 6, 8), world, random) {
            val type = random.nextInt(4)
            when (type) {
                0 -> {
                    if (random.nextInt(3) == 0) {
                        TAIGA_1.generate(world, random, it)
                    } else {
                        TAIGA_2.generate(world, random, it)
                    }
                }
                1 -> {
                    val height = this.range(random, 5, 10)
                    WorldGenTrees(false, height, JUNGLE_LOG, JUNGLE_LEAF, true).generate(world, random, it)
                }
                2 -> {
                    val height = this.range(random, 3, 6)
                    WorldGenTrees(false, height, BIRCH_LOG, BIRCH_LEAF, false).generate(world, random, it)
                }
                3 -> {
                    val height = this.range(random, 3, 6)
                    WorldGenTrees(false, height, OAK_LOG, OAK_LEAF, false).generate(world, random, it)
                }
            }
        }
    }
}
