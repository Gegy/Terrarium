package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.awt.Color;
import java.util.Random;

public class BareCover extends EarthCoverType {
    public BareCover() {
        super(new Color(0xFFF5D6));
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return Biomes.DESERT;
    }

    @Override
    public EarthSurfaceGenerator createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this);
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    private static class Surface extends EarthSurfaceGenerator {
        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
            ShortRaster heightRaster = this.context.getHeightRaster();
            UnsignedByteRaster slopeRaster = this.context.getSlopeRaster();
            this.iterateChunk((localX, localZ) -> {
                int slope = slopeRaster.getByte(localX, localZ);
                if (slope < MOUNTAINOUS_SLOPE && random.nextInt(250) == 0) {
                    int y = heightRaster.getShort(localX, localZ);
                    writer.set(localX, y + 1, localZ, DEAD_BUSH);
                }
            });
        }
    }

    private static class Decoration extends EarthDecorationGenerator {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random) {
            World world = this.context.getWorld();
            UnsignedByteRaster slopeRaster = this.context.getSlopeRaster();

            this.preventIntersection(5);

            this.decorateScatter(random, chunkPos, writer, this.range(random, -16, 1), (pos, localX, localZ) -> {
                if (slopeRaster.getByte(localX, localZ) < MOUNTAINOUS_SLOPE) {
                    OAK_TALL_SHRUB.generate(world, random, pos);
                }
            });

            this.stopIntersectionPrevention();
        }
    }
}
