package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.cover.CoverSelectors;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public final class EarthGrassComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 1271372693621632L;

    private final SpatialRandom random;

    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthData.COVER, Cover.NO);

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private static final WorldGenerator GRASS = new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
    private static final WorldGenerator FERN = new WorldGenTallGrass(BlockTallGrass.EnumType.FERN);

    private static final WorldGenDoublePlant TALL_GRASS = new WorldGenDoublePlant();
    private static final WorldGenDoublePlant TALL_FERN = new WorldGenDoublePlant();

    private static final WorldGenerator DEAD_BUSH = new WorldGenDeadBush();

    static {
        TALL_GRASS.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
        TALL_FERN.setPlantType(BlockDoublePlant.EnumPlantType.FERN);
    }

    public EarthGrassComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();
        Cover cover = this.coverSampler.sample(terrarium.getDataCache(), dataX, dataZ);
        if (cover.is(CoverMarkers.NO_VEGETATION)) return;

        int grassPerChunk = this.getGrassPerChunk(this.random, cover);
        int fernsPerChunk = this.getFernsPerChunk(cover);

        this.generateGrass(GRASS, writer, pos, grassPerChunk);
        this.generateGrass(FERN, writer, pos, fernsPerChunk);

        if (cover.is(CoverMarkers.DENSE_GRASS)) {
            this.generateGrass(TALL_GRASS, writer, pos, grassPerChunk / 2);
            this.generateGrass(TALL_FERN, writer, pos, fernsPerChunk / 2);
        }

        if (cover.is(CoverMarkers.HARSH)) {
            this.generateGrass(DEAD_BUSH, writer, pos, 1);
        }
    }

    private int getGrassPerChunk(Random random, Cover cover) {
        if (cover.is(CoverMarkers.DENSE_GRASS)) {
            return 4;
        }

        if (cover.is(CoverMarkers.FOREST) || cover.is(CoverSelectors.shrubs())) {
            return 2;
        }

        return random.nextInt(2);
    }

    private int getFernsPerChunk(Cover cover) {
        if (cover.is(CoverMarkers.FLOODED)) {
            return 3;
        } else if (cover.is(CoverMarkers.FOREST)) {
            return 1;
        }

        return 0;
    }

    private void generateGrass(WorldGenerator generator, ChunkPopulationWriter writer, CubicPos pos, int count) {
        World world = writer.getGlobal();
        int minX = pos.getCenterX();
        int minZ = pos.getCenterZ();

        for (int i = 0; i < count; i++) {
            this.mutablePos.setPos(
                    minX + this.random.nextInt(16),
                    0,
                    minZ + this.random.nextInt(16)
            );

            if (writer.getSurfaceMut(this.mutablePos)) {
                generator.generate(world, this.random, this.mutablePos);
            }
        }
    }
}
