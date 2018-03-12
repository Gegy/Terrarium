package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CoverSurfaceComposer implements SurfaceComposer {
    private static final long DEPTH_SEED = 6035435416693430887L;

    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    private final NoiseGeneratorPerlin depthNoise;
    private double[] depthBuffer = new double[16 * 16];

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final RegionComponentType<CoverRasterTileAccess> coverComponent;

    private final IBlockState replaceBlock;

    private final IBlockState[] coverBlockBuffer = ArrayUtils.defaulted(new IBlockState[16 * 16], AIR);
    private final IBlockState[] fillerBlockBuffer = ArrayUtils.defaulted(new IBlockState[16 * 16], AIR);

    private final Set<CoverType> coverTypes = new HashSet<>();
    private final Map<CoverType, CoverGenerator> generators = new EnumMap<>(CoverType.class);

    public CoverSurfaceComposer(
            World world,
            TerrariumWorldData worldData,
            RegionComponentType<CoverRasterTileAccess> coverComponent,
            RegionComponentType<ShortRasterTileAccess> heightComponent,
            RegionComponentType<ByteRasterTileAccess> slopeComponent,
            IBlockState replaceBlock
    ) {
        this.random = new Random(world.getSeed() ^ DEPTH_SEED);
        this.depthNoise = new NoiseGeneratorPerlin(this.random, 4);
        this.coverMap = new PseudoRandomMap(world.getSeed(), this.random.nextLong());

        this.coverComponent = coverComponent;

        this.replaceBlock = replaceBlock;

        // TODO: These shouldn't be initialized from the composers. Removes the need for the world and world data.
        GenerationRegionHandler regionHandler = worldData.getRegionHandler();
        for (CoverType coverType : CoverType.TYPES) {
            CoverGenerator generator = coverType.createGenerator();
            CoverRasterTileAccess coverRaster = regionHandler.getCachedChunkRaster(coverComponent);
            ShortRasterTileAccess heightRaster = regionHandler.getCachedChunkRaster(heightComponent);
            ByteRasterTileAccess slopeRaster = regionHandler.getCachedChunkRaster(slopeComponent);
            generator.initialize(world, coverRaster, heightRaster, slopeRaster, this.coverBlockBuffer, this.fillerBlockBuffer, false);
            this.generators.put(coverType, generator);
        }
    }

    @Override
    public void provideSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        CoverRasterTileAccess coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);

        this.depthBuffer = this.depthNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, 0.0625, 0.0625, 1.0);

        this.populateBlockCover(coverRaster, globalX, globalZ);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.coverMap.initPosSeed(localX + globalX, localZ + globalZ);
                this.coverColumn(primer, localX, localZ, this.depthBuffer[localX + localZ * 16]);
            }
        }
    }

    private void populateBlockCover(CoverRasterTileAccess coverBuffer, int globalX, int globalZ) {
        this.coverTypes.clear();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.coverTypes.add(coverBuffer.get(localX, localZ));
            }
        }

        this.coverMap.initPosSeed(globalX, globalZ);
        long randomSeed = this.coverMap.next();
        for (CoverType type : this.coverTypes) {
            CoverGenerator generator = this.generators.get(type);
            if (generator != null) {
                this.random.setSeed(randomSeed);

                generator.getCover(this.random, globalX, globalZ);
                generator.getFiller(this.random, globalX, globalZ);
            }
        }
    }

    private void coverColumn(ChunkPrimer primer, int localX, int localZ, double depthNoise) {
        int index = localX + localZ * 16;

        IBlockState currentTop = this.coverBlockBuffer[index];
        IBlockState currentFiller = this.fillerBlockBuffer[index];

        int depth = -1;
        int soilDepth = Math.max((int) (depthNoise / 3.0 + 3.0 + this.coverMap.nextDouble() * 0.25), 1);

        for (int localY = 255; localY >= 0; localY--) {
            IBlockState current = primer.getBlockState(localX, localY, localZ);
            while (current == AIR && --localY >= 0) {
                current = primer.getBlockState(localX, localY, localZ);
                depth = -1;
            }
            if (current == this.replaceBlock) {
                if (depth == -1) {
                    if (soilDepth <= 0) {
                        currentTop = AIR;
                        currentFiller = this.replaceBlock;
                    }
                    // TODO: What is the impact of this code? We have a separate composer for handling ocean
//                    if (localY < this.oceanHeight && currentTop.getMaterial() == Material.AIR) {
//                        currentTop = WATER;
//                    }
                    depth = soilDepth;

                    primer.setBlockState(localX, localY, localZ, currentTop);
                } else if (depth-- > 0) {
                    primer.setBlockState(localX, localY, localZ, currentFiller);
                } else {
                    break;
                }
            }
        }
    }

    public static class Parser implements InstanceObjectParser<SurfaceComposer> {
        @Override
        public SurfaceComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            RegionComponentType<ByteRasterTileAccess> slopeComponent = valueParser.parseComponentType(objectRoot, "slope_component", ByteRasterTileAccess.class);
            IBlockState replaceBlock = valueParser.parseBlockState(objectRoot, "replace_block");
            return new CoverSurfaceComposer(world, worldData, coverComponent, heightComponent, slopeComponent, replaceBlock);
        }
    }
}
