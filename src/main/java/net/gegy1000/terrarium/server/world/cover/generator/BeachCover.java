package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;

import java.util.Random;

public class BeachCover extends CoverGenerator {
    public BeachCover() {
        super(CoverType.BEACH);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        if (zone == LatitudinalZone.TROPICS) {
            this.decorateScatter(random, x, z, this.range(random, 0, 2), pos -> {
                int height = this.range(random, 6, 7);
                new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(this.world, random, pos);
            });
        }
    }
}
