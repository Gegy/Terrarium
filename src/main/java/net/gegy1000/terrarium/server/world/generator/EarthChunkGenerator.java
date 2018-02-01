package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobChunkPrimer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.map.system.chunk.CoverChunkDataProvider;
import net.gegy1000.terrarium.server.map.system.chunk.HeightChunkDataProvider;
import net.gegy1000.terrarium.server.map.system.component.TerrariumComponentTypes;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class EarthChunkGenerator extends CoveredChunkGenerator {
    public static final long ZONE_SCATTER_SEED = 3111632783370753319L;

    private final EarthGenerationSettings settings;
    private final boolean fastGenerate;

    private final World world;
    private final Random random;
    private final NoiseGeneratorPerlin coverNoise;
    private final PseudoRandomMap zoneScatterMap;

    private double[] depthBuffer = new double[256];
    private final IBlockState[] coverBlockBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);
    private final IBlockState[] fillerBlockBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);

    private Biome[] biomeBuffer = null;

    private final Lazy<GenerationRegionHandler> regionHandler;

    private final HeightChunkDataProvider heightProvider;
    private final CoverChunkDataProvider coverProvider;

    private final Map<CoverType, CoverGenerator> generators;

    public EarthChunkGenerator(World world, long seed, EarthGenerationSettings settings, boolean fastGenerate) {
        super(world, settings.getOceanHeight());
        this.world = world;

        this.settings = settings;
        this.fastGenerate = fastGenerate;

        this.random = new Random(seed);
        this.coverNoise = new NoiseGeneratorPerlin(this.random, 4);
        this.zoneScatterMap = new PseudoRandomMap(world, ZONE_SCATTER_SEED);

        this.heightProvider = new HeightChunkDataProvider(this.settings, TerrariumComponentTypes.HEIGHT);
        this.coverProvider = new CoverChunkDataProvider(this.settings, world, TerrariumComponentTypes.COVER);

        this.regionHandler = new Lazy<>(() -> {
            TerrariumWorldData capability = this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
            if (capability != null) {
                return capability.getRegionHandler();
            }
            throw new RuntimeException("Tried to load " + GenerationRegionHandler.class.getSimpleName() + " before it was present");
        });

        CoverType[] coverBuffer = this.coverProvider.getResultStore().getCoverData();
        int[] heightBuffer = this.heightProvider.getResultStore();
        this.generators = super.createGenerators(coverBuffer, heightBuffer, this.coverBlockBuffer, this.fillerBlockBuffer, false);
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer primer = this.generatePrimer(chunkX, chunkZ);
        this.biomeBuffer = this.populateBiomes(this.biomeBuffer, chunkX, chunkZ);

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
        int oceanHeight = this.settings.getOceanHeight();
        this.heightProvider.populate(this.regionHandler.get(), this.world, chunkX << 4, chunkZ << 4);

        int[] heightData = this.heightProvider.getResultStore();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int height = heightData[x + z * 16];
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
    }

    public Biome[] populateBiomes(Biome[] biomeBuffer, int chunkX, int chunkZ) {
        return this.world.getBiomeProvider().getBiomes(biomeBuffer, chunkX << 4, chunkZ << 4, 16, 16);
    }

    private void generateBiome(ChunkPrimer primer, int chunkX, int chunkZ) {
        double scale = 0.0625;

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.depthBuffer = this.coverNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, scale, scale, 1.0);

        this.coverProvider.populate(this.regionHandler.get(), this.world, globalX, globalZ);
        CoverChunkDataProvider.Data coverData = this.coverProvider.getResultStore();
        Set<CoverType> coverTypes = coverData.getTypes();

        for (CoverType type : coverTypes) {
            CoverGenerator generator = this.generators.get(type);
            if (generator != null) {
                generator.getCover(this.random, globalX, globalZ);
                generator.getFiller(this.random, globalX, globalZ);
            }
        }

        super.coverBiome(primer, this.coverBlockBuffer, this.fillerBlockBuffer, this.depthBuffer, globalX, globalZ);

        if (this.settings.decorate && !this.fastGenerate) {
            GlobPrimer globPrimer = new GlobChunkPrimer(primer);
            for (CoverType type : coverTypes) {
                CoverGenerator generator = this.generators.get(type);
                if (generator != null) {
                    generator.coverDecorate(globPrimer, this.random, globalX, globalZ);
                }
            }
        }
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.coverProvider.populate(this.regionHandler.get(), this.world, globalX + 8, globalZ + 8);

        CoverChunkDataProvider.Data coverData = this.coverProvider.getResultStore();

        LatitudinalZone zone = this.getLatitudinalZone(globalX, globalZ);

        CoverType cover = coverData.getCoverData()[136];
        Biome biome = cover.getBiome(zone);

        if (this.settings.decorate && !this.fastGenerate) {
            ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, chunkX, chunkZ, false);

            Set<CoverType> coverTypes = coverData.getTypes();
            for (CoverType type : coverTypes) {
                CoverGenerator generator = this.generators.get(type);
                if (generator != null) {
                    generator.decorate(this.random, zone, globalX + 8, globalZ + 8);
                }
            }

            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
                WorldEntitySpawner.performWorldGenSpawning(this.world, biome, globalX + 8, globalZ + 8, 16, 16, this.random);
            }

            ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, chunkX, chunkZ, false);
        }

        BlockFalling.fallInstantly = false;
    }

    private LatitudinalZone getLatitudinalZone(int x, int z) {
        this.zoneScatterMap.initPosSeed(x, z);
        int offset = this.zoneScatterMap.nextInt(128) - this.zoneScatterMap.nextInt(128);
        Coordinate chunkCoordinate = Coordinate.fromBlock(this.settings, x + 8, z + 8 + offset);
        return LatitudinalZone.get(chunkCoordinate);
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
