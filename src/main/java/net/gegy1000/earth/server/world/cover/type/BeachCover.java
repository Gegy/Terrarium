package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.ClimaticZone;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.awt.Color;
import java.util.Random;

public class BeachCover extends EarthCoverType implements BeachyCover {
    public BeachCover() {
        super(new Color(0xFFEBAE));
    }

    @Override
    public CoverSurfaceGenerator<EarthCoverContext> createSurfaceGenerator(EarthCoverContext context) {
        return new CoverSurfaceGenerator.Inherit<>(context, this);
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return Biomes.BEACH;
    }

    private static class Decoration extends EarthDecorationGenerator {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random) {
            ClimaticZone zone = this.context.getZone(chunkPos);
            if (zone == ClimaticZone.TROPICS) {
                World world = this.context.getWorld();
                this.decorateScatter(random, chunkPos, writer, this.range(random, 0, 2), (pos, localX, localZ) -> {
                    int height = this.range(random, 6, 7);
                    new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(world, random, pos);
                });
            }
        }
    }
}
