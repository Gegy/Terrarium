package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.world.World
import net.minecraft.world.gen.feature.WorldGenTrees
import java.util.Random

class ClosedNeedleleafEvergreen : Forest(GlobType.CLOSED_NEEDLELEAF_EVERGREEN) {
    override fun decorate(world: World, random: Random, x: Int, z: Int) {
        this.decorateScatter(x, z, this.range(random, 6, 8), world, random) {
            if (random.nextInt(8) != 0) {
                if (random.nextInt(3) == 0) {
                    TAIGA_1.generate(world, random, it)
                } else {
                    TAIGA_2.generate(world, random, it)
                }
            } else {
                val height = this.range(random, 5, 10)
                WorldGenTrees(false, height, JUNGLE_LOG, JUNGLE_LEAF, true).generate(world, random, it)
            }
        }
    }
}
