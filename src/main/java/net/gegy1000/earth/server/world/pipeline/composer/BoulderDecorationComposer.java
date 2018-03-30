package net.gegy1000.earth.server.world.pipeline.composer;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.feature.BoulderGenerator;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BoulderDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 8167086971552496758L;

    private static final BoulderGenerator BOULDER_GENERATOR = new BoulderGenerator(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), 0);

    private final RegionComponentType<ByteRasterTileAccess> slopeComponent;

    private final PseudoRandomMap decorationMap;
    private final Random random;

    public BoulderDecorationComposer(World world, RegionComponentType<ByteRasterTileAccess> slopeComponent) {
        this.slopeComponent = slopeComponent;

        this.decorationMap = new PseudoRandomMap(world, DECORATION_SEED);
        this.random = new Random();
    }

    @Override
    public void decorateChunk(World world, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.decorationMap.initPosSeed(globalX, globalZ);
        this.random.setSeed(this.decorationMap.next());

        ByteRasterTileAccess slopeRaster = regionHandler.getCachedChunkRaster(this.slopeComponent);

        for (int i = 0; i < 2; i++) {
            int localX = this.random.nextInt(16);
            int localZ = this.random.nextInt(16);

            if (this.random.nextInt(8) == 0) {
                if ((slopeRaster.getUnsigned(localX, localZ) & 0xFF) >= CoverGenerator.MOUNTAINOUS_SLOPE || this.random.nextInt(60) == 0) {
                    int spawnX = localX + globalX + 8;
                    int spawnZ = localZ + globalZ + 8;

                    BOULDER_GENERATOR.generate(world, this.random, world.getTopSolidOrLiquidBlock(new BlockPos(spawnX, 0, spawnZ)));
                }
            }
        }
    }

    public static class Parser implements InstanceObjectParser<DecorationComposer> {
        @Override
        public DecorationComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            RegionComponentType<ByteRasterTileAccess> slopeComponent = valueParser.parseComponentType(objectRoot, "slope_component", ByteRasterTileAccess.class);
            return new BoulderDecorationComposer(world, slopeComponent);
        }
    }
}