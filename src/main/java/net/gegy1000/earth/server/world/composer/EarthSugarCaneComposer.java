package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenerator;

public final class EarthSugarCaneComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 6192538672723303657L;

    private final SpatialRandom random;

    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthDataKeys.COVER, Cover.NO);

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private static final WorldGenerator GENERATOR = new WorldGenReed();

    public EarthSugarCaneComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();
        Cover cover = this.coverSampler.sample(dataCache, dataX, dataZ);

        if (cover.is(CoverMarkers.NO_VEGETATION)) return;

        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        int count = this.getCountPerChunk(cover);

        int minX = pos.getCenterX();
        int minZ = pos.getCenterZ();

        for (int i = 0; i < count; i++) {
            this.mutablePos.setPos(
                    minX + this.random.nextInt(16),
                    0,
                    minZ + this.random.nextInt(16)
            );

            if (writer.getSurfaceMut(this.mutablePos)) {
                GENERATOR.generate(writer.getGlobal(), this.random, this.mutablePos);
            }
        }
    }

    private int getCountPerChunk(Cover cover) {
        if (cover.is(CoverMarkers.FLOODED)) {
            return 3;
        }
        return this.random.nextInt(2);
    }
}
