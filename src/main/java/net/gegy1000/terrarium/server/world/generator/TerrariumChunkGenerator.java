package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobChunkPrimer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public abstract class TerrariumChunkGenerator implements IChunkGenerator {
    protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    protected static final IBlockState WATER = Blocks.WATER.getDefaultState();

    private static final int BEDROCK_HEIGHT = 5;

    protected final World world;
    protected final Random random;
    private final NoiseGeneratorPerlin coverNoise;

    private final int maxHeight;

    protected final Map<CoverType, CoverGenerator> generators = new EnumMap<>(CoverType.class);

    protected double[] depthBuffer = new double[256];
    protected final int[] heightBuffer = new int[256];
    protected final CoverType[] globBuffer = ArrayUtils.defaulted(new CoverType[256], CoverType.NO_DATA);
    protected final IBlockState[] coverBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);
    protected final IBlockState[] fillerBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);

    protected Biome[] biomeBuffer = null;

    public TerrariumChunkGenerator(World world, long seed, boolean debug) {
        this.world = world;

        this.random = new Random(seed);
        this.coverNoise = new NoiseGeneratorPerlin(this.random, 4);

        this.maxHeight = world.getHeight() - 1;

        for (CoverType coverType : CoverType.values()) {
            CoverGenerator generator = coverType.createGenerator();
            generator.initialize(world, this.globBuffer, this.heightBuffer, this.coverBuffer, this.fillerBuffer, debug);
            this.generators.put(coverType, generator);
        }
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer primer = this.generatePrimer(chunkX, chunkZ);
        this.biomeBuffer = this.generateBiomes(this.biomeBuffer, chunkX, chunkZ);

        Chunk chunk = new Chunk(this.world, primer, chunkX, chunkZ);

        if (this.biomeBuffer != null) {
            byte[] biomeArray = chunk.getBiomeArray();
            for (int i = 0; i < this.biomeBuffer.length; i++) {
                biomeArray[i] = (byte) Biome.getIdForBiome(this.biomeBuffer[i]);
            }
        }

        chunk.generateSkylightMap();

        return chunk;
    }

    public ChunkPrimer generatePrimer(int chunkX, int chunkZ) {
        this.random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

        ChunkPrimer primer = new ChunkPrimer();
        this.populateBlocks(primer, chunkX, chunkZ);
        this.generateBiome(primer, chunkX, chunkZ);

        return primer;
    }

    public Biome[] generateBiomes(Biome[] biomeBuffer, int chunkX, int chunkZ) {
        return this.world.getBiomeProvider().getBiomes(biomeBuffer, chunkX << 4, chunkZ << 4, 16, 16);
    }

    private void populateBlocks(ChunkPrimer primer, int chunkX, int chunkZ) {
        int oceanHeight = this.getOceanHeight();
        this.populateHeightRegion(this.heightBuffer, chunkX, chunkZ);

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int height = this.heightBuffer[x + z * 16];
                for (int y = 1; y <= height; y++) {
                    primer.setBlockState(x, y, z, STONE);
                }
                if (height < oceanHeight) {
                    for (int y = height + 1; y <= oceanHeight; y++) {
                        primer.setBlockState(x, y, z, WATER);
                    }
                }
            }
        }

        if (!this.shouldFastGenerate()) {

        }
    }

    private void generateBiome(ChunkPrimer primer, int chunkX, int chunkZ) {
        double scale = 0.0625;

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.depthBuffer = this.coverNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, scale, scale, 1.0);

        this.populateCoverRegion(this.globBuffer, chunkX, chunkZ);

        Set<CoverType> types = new HashSet<>();
        Collections.addAll(types, this.globBuffer);

        for (CoverType type : types) {
            CoverGenerator generator = this.generators.get(type);
            if (generator != null) {
                generator.getCover(this.random, globalX, globalZ);
                generator.getFiller(this.random, globalX, globalZ);
            }
        }

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int index = x + z * 16;
                IBlockState cover = this.coverBuffer[index];
                IBlockState filler = this.fillerBuffer[index];
                double noise = this.depthBuffer[index];
                this.generateBiomeTerrain(cover, filler, this.random, primer, globalX + x, globalZ + z, noise);
            }
        }

        if (this.shouldDecorate() && !this.shouldFastGenerate()) {
            GlobPrimer globPrimer = new GlobChunkPrimer(primer);
            for (CoverType type : types) {
                CoverGenerator generator = this.generators.get(type);
                if (generator != null) {
                    generator.coverDecorate(globPrimer, this.random, globalX, globalZ);
                }
            }
        }
    }

    private void generateBiomeTerrain(IBlockState topBlock, IBlockState fillerBlock, Random random, ChunkPrimer primer, int x, int z, double noise) {
        int oceanHeight = this.getOceanHeight();

        IBlockState currentTop = topBlock;
        IBlockState currentFiller = fillerBlock;

        int depth = -1;
        int soilDepth = Math.max((int) (noise / 3.0 + 3.0 + random.nextDouble() * 0.25), 1);

        int localX = x & 15;
        int localZ = z & 15;
        int localY = this.maxHeight;
        while (localY >= 0) {
            if (localY < BEDROCK_HEIGHT) {
                if (localY == 0 || localY <= random.nextInt(BEDROCK_HEIGHT)) {
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
                        if (localY < oceanHeight && currentTop.getMaterial() == Material.AIR) {
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

    @Override
    public void populate(int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;

        int x = chunkX << 4;
        int z = chunkZ << 4;

        this.populateCoverDirect(this.globBuffer, x + 8, z + 8, 16, 16);

        this.initializeChunkSeed(chunkX, chunkZ);

        LatitudinalZone zone = this.getLatitudinalZone(x, z);

        CoverType cover = this.globBuffer[136];
        Biome biome = cover.getBiome(zone);

        if (this.shouldDecorate() && !this.shouldFastGenerate()) {
            ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, chunkX, chunkZ, false);

            Set<CoverType> types = new HashSet<>();
            Collections.addAll(types, this.globBuffer);

            for (CoverType type : types) {
                CoverGenerator generator = this.generators.get(type);
                if (generator != null) {
                    generator.decorate(this.random, zone, x + 8, z + 8);
                }
            }

            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
                WorldEntitySpawner.performWorldGenSpawning(this.world, biome, x + 8, z + 8, 16, 16, this.random);
            }

            ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, chunkX, chunkZ, false);
        }

        BlockFalling.fallInstantly = false;
    }

    protected void initializeChunkSeed(int chunkX, int chunkZ) {
        this.random.setSeed(this.world.getSeed());
        long seedX = this.random.nextLong() / 2L * 2L + 1L;
        long seedZ = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed(chunkX * seedX + chunkZ * seedZ ^ this.world.getSeed());
    }

    protected abstract void populateHeightRegion(int[] heightBuffer, int chunkX, int chunkZ);

    protected void populateCoverRegion(CoverType[] coverBuffer, int chunkX, int chunkZ) {
        this.populateCoverDirect(coverBuffer, chunkX << 4, chunkZ << 4, 16, 16);
    }

    protected abstract void populateCoverDirect(CoverType[] coverBuffer, int globalX, int globalZ, int width, int height);

    protected abstract LatitudinalZone getLatitudinalZone(int x, int z);

    protected abstract int getOceanHeight();

    protected abstract boolean shouldDecorate();

    protected abstract boolean shouldFastGenerate();
}
