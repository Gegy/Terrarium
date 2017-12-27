package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobChunkPrimer;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class EarthChunkGenerator implements IChunkGenerator {
    private static final IBlockState STONE = Blocks.STONE.getDefaultState();
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();

    private static final int BEDROCK_HEIGHT = 5;

    private final World world;
    private final Random random;
    private final NoiseGeneratorPerlin coverNoise;
    private final EarthGenerationSettings settings;

    private final int maxHeight;

    private final boolean fastGenerate;

    private final Map<GlobType, GlobGenerator> generators = new EnumMap<>(GlobType.class);

    private double[] depthBuffer = new double[256];
    private int[] heightBuffer = new int[256];
    private GlobType[] globBuffer = ArrayUtils.defaulted(new GlobType[256], GlobType.NO_DATA);
    private IBlockState[] coverBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);
    private IBlockState[] fillerBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);

    private Biome[] biomeBuffer = null;

    private final Lazy<EarthGenerationHandler> generationHandler = new Lazy<>(() -> {
        TerrariumWorldData capability = EarthChunkGenerator.this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (capability != null) {
            return capability.getGenerationHandler();
        }
        throw new RuntimeException("Tried to load EarthGenerationHandler before it was present");
    });

    public EarthChunkGenerator(World world, long seed, String settingsString, boolean fastGenerate) {
        this.world = world;
        this.fastGenerate = fastGenerate;
        this.random = new Random(seed);
        this.coverNoise = new NoiseGeneratorPerlin(this.random, 4);
        this.settings = EarthGenerationSettings.deserialize(settingsString);

        this.maxHeight = world.getHeight() - 1;

        for (GlobType globType : GlobType.values()) {
            GlobGenerator generator = globType.createGenerator();
            generator.initialize(world, this.globBuffer, this.heightBuffer, this.coverBuffer, this.fillerBuffer);
            this.generators.put(globType, generator);
        }
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer primer = this.generatePrimer(chunkX, chunkZ);
        this.biomeBuffer = this.world.getBiomeProvider().getBiomes(this.biomeBuffer, chunkX << 4, chunkZ << 4, 16, 16);

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

    private void populateBlocks(ChunkPrimer primer, int chunkX, int chunkZ) {
        int oceanHeight = this.generationHandler.get().getOceanHeight();
        this.generationHandler.get().populateHeightRegion(this.heightBuffer, chunkX, chunkZ);

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

        if (!this.fastGenerate) {

        }
    }

    private void generateBiome(ChunkPrimer primer, int chunkX, int chunkZ) {
        double scale = 0.0625;

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.depthBuffer = this.coverNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, scale, scale, 1.0);

        this.generationHandler.get().populateGlobRegion(this.globBuffer, chunkX, chunkZ);

        Set<GlobType> types = new HashSet<>();
        Collections.addAll(types, this.globBuffer);

        for (GlobType type : types) {
            GlobGenerator generator = this.generators.get(type);
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

        if (this.settings.decorate && !this.fastGenerate) {
            GlobChunkPrimer globPrimer = new GlobChunkPrimer(primer);
            for (GlobType type : types) {
                GlobGenerator generator = this.generators.get(type);
                if (generator != null) {
                    IntCache.resetIntCache();
                    generator.coverDecorate(globPrimer, this.random, globalX, globalZ);
                }
            }
        }
    }

    private void generateBiomeTerrain(IBlockState topBlock, IBlockState fillerBlock, Random random, ChunkPrimer primer, int x, int z, double noise) {
        int oceanHeight = this.generationHandler.get().getOceanHeight();

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

        this.generationHandler.get().populateGlobRegion(this.globBuffer, chunkX, chunkZ);

        int x = chunkX << 4;
        int z = chunkZ << 4;

        this.random.setSeed(this.world.getSeed());
        long seedX = this.random.nextLong() / 2L * 2L + 1L;
        long seedZ = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed(chunkX * seedX + chunkZ * seedZ ^ this.world.getSeed());

        GlobType glob = this.globBuffer[255];
        Biome biome = glob.getBiome();

        if (this.settings.decorate && !this.fastGenerate) {
            ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, chunkX, chunkZ, false);

            GlobGenerator generator = this.generators.get(glob);
            if (generator != null) {
                generator.decorate(this.random, x + 8, z + 8);
            }

            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
                WorldEntitySpawner.performWorldGenSpawning(this.world, biome, x + 8, z + 8, 16, 16, this.random);
            }

            ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, chunkX, chunkZ, false);
        }

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getNearestStructurePos(World world, String structureName, BlockPos pos, boolean findUnexplored) {
        return pos;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        return false;
    }
}
