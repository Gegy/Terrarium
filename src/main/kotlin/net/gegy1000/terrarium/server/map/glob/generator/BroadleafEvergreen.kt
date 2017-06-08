package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.world.World
import net.minecraft.world.gen.feature.WorldGenTrees
import java.util.Random

class BroadleafEvergreen : Forest(GlobType.BROADLEAF_EVERGREEN) {
    override fun decorate(world: World, random: Random, x: Int, z: Int) {
        this.decorateScatter(x, z, this.range(random, 2, 4), world, random) {
            var height = this.range(random, 3, 6)
            if (random.nextInt(10) == 0) {
                height += 3
            }
            WorldGenTrees(false, height, OAK_LOG, OAK_LEAF, false).generate(world, random, it)
        }

        this.decorateScatter(x, z, this.range(random, 0, 1), world, random) {
            val height = this.range(random, 5, 10)
            WorldGenTrees(false, height, JUNGLE_LOG, JUNGLE_LEAF, true).generate(world, random, it)
        }
    }
}
