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
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public final class EarthGrassComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 1271372693621632L;

    private final SpatialRandom random;

    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthDataKeys.COVER, Cover.NO);

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private static final WorldGenerator TALL_GRASS = new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
    private static final WorldGenDoublePlant DOUBLE_GRASS = new WorldGenDoublePlant();

    static {
        DOUBLE_GRASS.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
    }

    public EarthGrassComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();
        Cover cover = this.coverSampler.sample(dataCache, dataX, dataZ);

        int minX = pos.getCenterX();
        int minZ = pos.getCenterZ();

        int grassPerChunk = this.getGrassPerChunk(this.random, cover);
        this.generateGrass(writer, minX, minZ, grassPerChunk);

        if (cover.is(CoverMarkers.DENSE_GRASS)) {
            this.generateDoubleGrass(writer, minX, minZ, 3);
        }
    }

    private int getGrassPerChunk(Random random, Cover cover) {
        if (cover.is(CoverMarkers.DENSE_GRASS)) {
            return 4;
        }

        if (cover.is(CoverMarkers.FOREST) || cover.is(CoverMarkers.SHRUBS)) {
            return 2;
        }

        return random.nextInt(2);
    }

    private void generateGrass(ChunkPopulationWriter writer, int minX, int minZ, int count) {
        World world = writer.getGlobal();

        for (int i = 0; i < count; i++) {
            this.mutablePos.setPos(
                    minX + this.random.nextInt(16),
                    0,
                    minZ + this.random.nextInt(16)
            );

            if (writer.getSurfaceMut(this.mutablePos)) {
                TALL_GRASS.generate(world, this.random, this.mutablePos);
            }
        }
    }

    private void generateDoubleGrass(ChunkPopulationWriter writer, int minX, int minZ, int count) {
        World world = writer.getGlobal();

        for (int i = 0; i < count; i++) {
            this.mutablePos.setPos(
                    minX + this.random.nextInt(16),
                    0,
                    minZ + this.random.nextInt(16)
            );

            if (writer.getSurfaceMut(this.mutablePos)) {
                DOUBLE_GRASS.generate(world, this.random, this.mutablePos);
            }
        }
    }
}
