package net.gegy1000.terrarium.server.world.generator.debug;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobChunkPrimer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.generator.CoveredChunkGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CoverDebugChunkGenerator extends CoveredChunkGenerator {
    private static final int LAND_HEIGHT = 62;

    private final World world;
    private final Random random;
    private final NoiseGeneratorPerlin coverNoise;

    private double[] depthBuffer = new double[256];

    private final Set<CoverType> coverTypes = new HashSet<>();
    private final CoverType[] coverBuffer = ArrayUtils.defaulted(new CoverType[256], CoverType.DEBUG);
    private final IBlockState[] coverBlockBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);
    private final IBlockState[] fillerBlockBuffer = ArrayUtils.defaulted(new IBlockState[256], STONE);

    private Biome[] biomeBuffer = null;

    private final Map<CoverType, CoverGenerator> generators;

    public CoverDebugChunkGenerator(World world, long seed) {
        super(world, 0);
        this.world = world;

        this.random = new Random(seed);
        this.coverNoise = new NoiseGeneratorPerlin(this.random, 4);

        int[] heightBuffer = new int[256];
        byte[] slopeBuffer = new byte[256];
        Arrays.fill(heightBuffer, LAND_HEIGHT);

        this.generators = super.createGenerators(this.coverBuffer, heightBuffer, slopeBuffer, this.coverBlockBuffer, this.fillerBlockBuffer, true);
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

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                for (int y = 1; y <= LAND_HEIGHT; y++) {
                    primer.setBlockState(x, y, z, STONE);
                }
            }
        }

        this.generateBiome(primer, chunkX, chunkZ);

        return primer;
    }

    private void generateBiome(ChunkPrimer primer, int chunkX, int chunkZ) {
        double scale = 0.0625;

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.depthBuffer = this.coverNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, scale, scale, 1.0);

        this.populateCover(globalX, globalZ);

        for (CoverType type : this.coverTypes) {
            CoverGenerator generator = this.generators.get(type);
            if (generator != null) {
                generator.getCover(this.random, globalX, globalZ);
                generator.getFiller(this.random, globalX, globalZ);
            }
        }

        super.coverBiome(primer, this.coverBlockBuffer, this.fillerBlockBuffer, this.depthBuffer, globalX, globalZ);

        GlobPrimer globPrimer = new GlobChunkPrimer(primer);
        for (CoverType type : this.coverTypes) {
            CoverGenerator generator = this.generators.get(type);
            if (generator != null) {
                generator.coverDecorate(globPrimer, this.random, globalX, globalZ);
            }
        }
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.populateCover(globalX + 8, globalZ + 8);

        LatitudinalZone zone = DebugMap.getCover(globalX + 16, globalZ + 16).getZone();

        for (CoverType type : this.coverTypes) {
            CoverGenerator generator = this.generators.get(type);
            if (generator != null) {
                generator.decorate(this.random, zone, globalX + 8, globalZ + 8);
            }
        }
    }

    private void populateCover(int originX, int originZ) {
        this.coverTypes.clear();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                CoverType cover = DebugMap.getCover(originX + localX, originZ + localZ).getCoverType();

                this.coverTypes.add(cover);
                this.coverBuffer[localX + localZ * 16] = cover;
            }
        }
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return Collections.emptyList();
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
