package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.world.gen.feature.WorldGenTrees
import java.util.Random

class OpenNeedleleaf : Forest(GlobType.OPEN_NEEDLELEAF) {
    override fun decorate(random: Random, x: Int, z: Int) {
        this.decorateScatter(x, z, this.range(random, 6, 8), random) {
            if (random.nextInt(8) != 0) {
                if (random.nextInt(3) == 0) {
                    TAIGA_1.generate(this.world, random, it)
                } else {
                    TAIGA_2.generate(this.world, random, it)
                }
            } else if (random.nextBoolean()) {
                val height = this.range(random, 5, 10)
                WorldGenTrees(false, height, JUNGLE_LOG, JUNGLE_LEAF, true).generate(this.world, random, it)
            } else {
                var height = this.range(random, 3, 6)
                if (random.nextInt(10) == 0) {
                    height += 3
                }
                WorldGenTrees(false, height, BIRCH_LOG, BIRCH_LEAF, false).generate(this.world, random, it)
            }
        }
    }
}