package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverChunkPrimer;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CoverSurfaceComposer implements SurfaceComposer {
    private static final long DEPTH_SEED = 6035435416693430887L;

    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    private final NoiseGeneratorPerlin depthNoise;
    private double[] depthBuffer = new double[16 * 16];

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final RegionComponentType<CoverRasterTile> coverComponent;

    private final boolean decorate;
    private final IBlockState replaceBlock;

    private final IBlockState[] coverBlockBuffer = ArrayUtils.defaulted(new IBlockState[16 * 16], AIR);
    private final IBlockState[] fillerBlockBuffer = ArrayUtils.defaulted(new IBlockState[16 * 16], AIR);

    private final List<CoverGenerationContext> context;
    private final Map<CoverType<?>, CoverSurfaceGenerator<?>> generators;
    private final Set<CoverType<?>> localCoverTypes = new HashSet<>();

    public CoverSurfaceComposer(
            World world,
            RegionComponentType<CoverRasterTile> coverComponent,
            List<ConstructedCover<?>> coverTypes,
            boolean decorate,
            IBlockState replaceBlock
    ) {
        this.random = new Random(world.getWorldInfo().getSeed() ^ DEPTH_SEED);
        this.depthNoise = new NoiseGeneratorPerlin(this.random, 4);
        this.coverMap = new PseudoRandomMap(world.getWorldInfo().getSeed(), this.random.nextLong());

        this.coverComponent = coverComponent;

        this.decorate = decorate;
        this.replaceBlock = replaceBlock;

        this.context = coverTypes.stream().map(ConstructedCover::getContext).collect(Collectors.toList());
        this.generators = coverTypes.stream().collect(Collectors.toMap(ConstructedCover::getType, ConstructedCover::createSurfaceGenerator));
    }

    @Override
    public void composeSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        CoverRasterTile coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);

        this.depthBuffer = this.depthNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, 0.0625, 0.0625, 1.0);

        for (CoverGenerationContext context : this.context) {
            context.prepareChunk(regionHandler);
        }

        this.populateBlockCover(coverRaster, globalX, globalZ);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.coverMap.initPosSeed(localX + globalX, localZ + globalZ);
                this.coverColumn(primer, localX, localZ, this.depthBuffer[localX + localZ * 16]);
            }
        }

        if (this.decorate) {
            this.coverMap.initPosSeed(globalX, globalZ);
            long randomSeed = this.coverMap.next();

            for (CoverType<?> type : this.localCoverTypes) {
                CoverSurfaceGenerator<?> generator = this.generators.get(type);
                if (generator != null) {
                    this.random.setSeed(randomSeed);
                    this.random.setSeed(this.random.nextLong());
                    generator.decorate(globalX, globalZ, new CoverChunkPrimer(primer), this.random);
                } else {
                    Terrarium.LOGGER.warn("Tried to generate with non-registered cover: {}", type);
                }
            }
        }
    }

    private void populateBlockCover(CoverRasterTile coverBuffer, int globalX, int globalZ) {
        this.localCoverTypes.clear();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.localCoverTypes.add(coverBuffer.get(localX, localZ));
            }
        }

        this.coverMap.initPosSeed(globalX, globalZ);
        long randomSeed = this.coverMap.next();
        for (CoverType<?> type : this.localCoverTypes) {
            CoverSurfaceGenerator<?> generator = this.generators.get(type);
            if (generator != null) {
                this.random.setSeed(randomSeed);

                generator.populateBlockCover(this.random, globalX, globalZ, this.coverBlockBuffer);
                generator.populateBlockFiller(this.random, globalX, globalZ, this.fillerBlockBuffer);
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
}
