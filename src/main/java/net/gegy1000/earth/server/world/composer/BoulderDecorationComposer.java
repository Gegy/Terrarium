package net.gegy1000.earth.server.world.composer;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.feature.BoulderGenerator;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UByteRaster;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BoulderDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 8167086971552496758L;

    private static final BoulderGenerator BOULDER_GENERATOR = new BoulderGenerator(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), 0);

    private static final int MIN_SLOPE = 45;

    private final UByteRaster.Sampler slopeSampler;

    private final SpatialRandom random;

    public BoulderDecorationComposer(World world, DataKey<UByteRaster> slopeKey) {
        this.slopeSampler = UByteRaster.sampler(slopeKey);

        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        int globalX = pos.getCenterX();
        int globalY = pos.getCenterY();
        int globalZ = pos.getCenterZ();

        this.random.setSeed(globalX, globalY, globalZ);

        DataView view = DataView.square(globalX, globalZ, 16);
        UByteRaster slopeRaster = this.slopeSampler.sample(dataCache, view);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 2; i++) {
            if (this.random.nextInt(16) == 0) {
                int localX = this.random.nextInt(16);
                int localZ = this.random.nextInt(16);

                int spawnX = localX + globalX;
                int spawnZ = localZ + globalZ;

                int slope = slopeRaster.get(localX, localZ);
                if (slope >= MIN_SLOPE || this.random.nextInt(30) == 0) {
                    mutablePos.setPos(spawnX, 0, spawnZ);
                    BlockPos surface = writer.getSurface(mutablePos);
                    if (surface != null) {
                        BOULDER_GENERATOR.generate(writer.getGlobal(), this.random, surface);
                    }
                }
            }
        }
    }
}
