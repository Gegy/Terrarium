package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.EnumMap;
import java.util.Map;

public abstract class CoveredChunkGenerator implements IChunkGenerator {
    private static final long COVER_SEED = 7302097788252858954L;

    protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    protected static final IBlockState WATER = Blocks.WATER.getDefaultState();

    protected static final int BEDROCK_HEIGHT = 5;

    private final World world;
    private final int oceanHeight;

    private final PseudoRandomMap coverMap;

    protected CoveredChunkGenerator(World world, int oceanHeight) {
        this.world = world;
        this.oceanHeight = oceanHeight;

        this.coverMap = new PseudoRandomMap(world, COVER_SEED);
    }

    protected final Map<CoverType, CoverGenerator> createGenerators(CoverType[] coverBuffer, int[] heightBuffer, byte[] slopeBuffer, IBlockState[] coverBlockBuffer, IBlockState[] fillerBlockBuffer, boolean debug) {
        Map<CoverType, CoverGenerator> generators = new EnumMap<>(CoverType.class);
        for (CoverType coverType : CoverType.values()) {
            CoverGenerator generator = coverType.createGenerator();
            generator.initialize(this.world, coverBuffer, heightBuffer, slopeBuffer, coverBlockBuffer, fillerBlockBuffer, debug);
            generators.put(coverType, generator);
        }
        return generators;
    }

    protected final void coverBiome(ChunkPrimer primer, IBlockState[] coverBlockBuffer, IBlockState[] fillerBlockBuffer, double[] depthBuffer, int globalX, int globalZ) {
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int index = localX + localZ * 16;
                IBlockState cover = coverBlockBuffer[index];
                IBlockState filler = fillerBlockBuffer[index];
                double noise = depthBuffer[index];
                this.coverBiomeTerrain(cover, filler, primer, globalX + localX, globalZ + localZ, noise);
            }
        }
    }

    private void coverBiomeTerrain(IBlockState topBlock, IBlockState fillerBlock, ChunkPrimer primer, int globalX, int globalZ, double noise) {
        this.coverMap.initPosSeed(globalX, globalZ);

        IBlockState currentTop = topBlock;
        IBlockState currentFiller = fillerBlock;

        int depth = -1;
        int soilDepth = Math.max((int) (noise / 3.0 + 3.0 + this.coverMap.nextDouble() * 0.25), 1);

        int localX = globalX & 15;
        int localZ = globalZ & 15;
        int localY = this.world.getHeight() - 1;

        while (localY >= 0) {
            if (localY < BEDROCK_HEIGHT) {
                if (localY == 0 || localY <= this.coverMap.nextInt(BEDROCK_HEIGHT)) {
                    primer.setBlockState(localX, localY, localZ, BEDROCK);
                }
            } else {
                IBlockState current = primer.getBlockState(localX, localY, localZ);
                while (current.getMaterial() == Material.AIR && localY >= BEDROCK_HEIGHT) {
                    current = primer.getBlockState(localX, --localY, localZ);
                    depth = -1;
                }
                if (current == STONE) {
                    if (depth == -1) {
                        if (soilDepth <= 0) {
                            currentTop = AIR;
                            currentFiller = STONE;
                        }
                        if (localY < this.oceanHeight && currentTop.getMaterial() == Material.AIR) {
                            currentTop = WATER;
                        }
                        depth = soilDepth;

                        primer.setBlockState(localX, localY, localZ, currentTop);
                    } else if (depth-- > 0) {
                        primer.setBlockState(localX, localY, localZ, currentFiller);
                    } else {
                        localY = BEDROCK_HEIGHT;
                    }
                }
            }
            localY--;
        }
    }
}
