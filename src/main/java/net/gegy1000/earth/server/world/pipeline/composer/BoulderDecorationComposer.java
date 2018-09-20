package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.feature.BoulderGenerator;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BoulderDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 8167086971552496758L;

    private static final BoulderGenerator BOULDER_GENERATOR = new BoulderGenerator(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), 0);

    private final RegionComponentType<UnsignedByteRasterTile> slopeComponent;

    private final PseudoRandomMap decorationMap;
    private final Random random;

    public BoulderDecorationComposer(World world, RegionComponentType<UnsignedByteRasterTile> slopeComponent) {
        this.slopeComponent = slopeComponent;

        this.decorationMap = new PseudoRandomMap(world, DECORATION_SEED);
        this.random = new Random(0);
    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        int globalX = pos.getMinX();
        int globalY = pos.getMinY();
        int globalZ = pos.getMinZ();

        this.decorationMap.initPosSeed(globalX, globalY, globalZ);
        this.random.setSeed(this.decorationMap.next());

        UnsignedByteRasterTile slopeRaster = regionHandler.getCachedChunkRaster(this.slopeComponent);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 2; i++) {
            int localX = this.random.nextInt(16);
            int localZ = this.random.nextInt(16);

            if (this.random.nextInt(32) == 0) {
                if (slopeRaster.getByte(localX, localZ) >= CoverGenerator.MOUNTAINOUS_SLOPE || this.random.nextInt(60) == 0) {
                    int spawnX = localX + globalX + 8;
                    int spawnZ = localZ + globalZ + 8;

                    mutablePos.setPos(spawnX, 0, spawnZ);
                    BlockPos surface = writer.getSurface(mutablePos);
                    if (surface != null) {
                        BOULDER_GENERATOR.generate(writer.getGlobal(), this.random, surface);
                    }
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.slopeComponent };
    }
}
