package dev.gegy.terrarium.world.generator.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.earth.EarthAttachments;
import dev.gegy.terrarium.backend.earth.EarthConfiguration;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.raster.ShortRaster;
import dev.gegy.terrarium.backend.tile.GuavaTileCache;
import dev.gegy.terrarium.world.GeoProvider;
import dev.gegy.terrarium.world.GeoProviderHolder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class EarthChunkGenerator extends GeoChunkGenerator {
    public static final MapCodec<EarthChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(c -> c.biomeSource),
            Codec.INT.fieldOf("min_y").forGetter(EarthChunkGenerator::getMinY),
            Codec.INT.fieldOf("height").forGetter(EarthChunkGenerator::getGenDepth),
            EarthConfiguration.CODEC.forGetter(c -> c.configuration)
    ).apply(i, EarthChunkGenerator::new));

    private static final SurfaceRules.RuleSource SURFACE_RULE = SurfaceRuleData.overworld();

    private final int minY;
    private final int height;
    private final int maxY;

    private final EarthConfiguration configuration;
    private final float heightScale;

    private final BlockState fillBlock = Blocks.STONE.defaultBlockState();
    private final BlockState fluidBlock = Blocks.WATER.defaultBlockState();

    public EarthChunkGenerator(final BiomeSource biomeSource, final int minY, final int height, final EarthConfiguration configuration) {
        super(biomeSource);
        this.minY = minY;
        this.height = height;
        maxY = minY + height - 1;

        this.configuration = configuration;
        heightScale = configuration.heightScale() / configuration.projection().idealMetersPerBlock();
    }

    public EarthConfiguration configuration() {
        return configuration;
    }

    @Override
    public GeoProvider createGeoProvider() {
        return new GeoProvider(EarthLayers.create(
                Terrarium.createTiles(new GuavaTileCache(Duration.ofSeconds(30), 256)),
                configuration.projection(),
                Util.backgroundExecutor()
        ));
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(final WorldGenRegion region, final long seed, final RandomState randomState, final BiomeManager biomes, final StructureManager structures, final ChunkAccess chunk, final GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(final WorldGenRegion region, final StructureManager structures, final RandomState randomState, final ChunkAccess chunk) {
        final GeoChunk geoChunk = getGeoChunk(chunk);

        final WorldGenerationContext context = new WorldGenerationContext(this, region);
        final BiomeManager biomeManager = region.getBiomeManager();
        final Registry<Biome> biomeRegistry = region.registryAccess().registryOrThrow(Registries.BIOME);
        final NoiseChunk noiseChunk = getOrCreateDummyNoiseChunk(randomState, chunk, geoChunk);
        randomState.surfaceSystem().buildSurface(randomState, biomeManager, biomeRegistry, false, context, chunk, noiseChunk, SURFACE_RULE);
    }

    private NoiseChunk getOrCreateDummyNoiseChunk(final RandomState randomState, final ChunkAccess chunk, final GeoChunk geoChunk) {
        final ShortRaster elevation = geoChunk.get(EarthAttachments.ELEVATION);
        final DummyNoiseChunkFactory.SurfaceSampler surfaceSampler;
        if (elevation != null) {
            final ChunkPos chunkPos = chunk.getPos();
            final int minBlockX = chunkPos.getMinBlockX();
            final int minBlockZ = chunkPos.getMinBlockZ();
            surfaceSampler = (x, z) -> {
                // Surface builders query slightly out of range, but it's good enough to fudge and clamp it
                final int relativeX = Mth.clamp(x - minBlockX, 0, SectionPos.SECTION_MAX_INDEX);
                final int relativeZ = Mth.clamp(z - minBlockZ, 0, SectionPos.SECTION_MAX_INDEX);
                return transformElevationToY(elevation.getInt(relativeX, relativeZ));
            };
        } else {
            surfaceSampler = (x, z) -> minY;
        }
        return DummyNoiseChunkFactory.getOrCreate(chunk, randomState, surfaceSampler);
    }

    @Override
    public void spawnOriginalMobs(final WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return height;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final Blender blender, final RandomState randomState, final StructureManager structures, final ChunkAccess chunk) {
        final ShortRaster elevation = getGeoChunk(chunk).get(EarthAttachments.ELEVATION);
        if (elevation != null) {
            fillSurface(chunk, elevation, fillBlock, fluidBlock, getSeaLevel());
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private void fillSurface(final ChunkAccess chunk, final ShortRaster elevationRaster, final BlockState fillBlock, final BlockState fluidBlock, final int seaLevel) {
        final Heightmap oceanFloorHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        final Heightmap worldSurfaceHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        final int[] surfaceMap = new int[SectionPos.SECTION_SIZE * SectionPos.SECTION_SIZE];
        int maxY = seaLevel;
        for (int z = 0; z < SectionPos.SECTION_SIZE; z++) {
            for (int x = 0; x < SectionPos.SECTION_SIZE; x++) {
                final int surfaceY = transformElevationToY(elevationRaster.getInt(x, z));
                surfaceMap[x + z * SectionPos.SECTION_SIZE] = surfaceY;
                oceanFloorHeightmap.update(x, surfaceY, z, fillBlock);
                worldSurfaceHeightmap.update(x, Math.max(surfaceY, seaLevel), z, fluidBlock);
                if (surfaceY > maxY) {
                    maxY = surfaceY;
                }
            }
        }

        final int maxSectionY = SectionPos.blockToSectionCoord(maxY);
        final int minSectionY = chunk.getMinSection();
        for (int sectionY = maxSectionY; sectionY >= minSectionY; sectionY--) {
            final int sectionBottomY = SectionPos.sectionToBlockCoord(sectionY);
            final int sectionTopY = SectionPos.sectionToBlockCoord(sectionY, SectionPos.SECTION_MAX_INDEX);
            final LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
            for (int z = 0; z < SectionPos.SECTION_SIZE; z++) {
                for (int x = 0; x < SectionPos.SECTION_SIZE; x++) {
                    final int surfaceY = surfaceMap[x + z * SectionPos.SECTION_SIZE];
                    final int topY = Math.max(surfaceY, seaLevel);
                    for (int y = Math.min(topY, sectionTopY); y >= sectionBottomY; y--) {
                        final BlockState block = y > surfaceY ? fluidBlock : fillBlock;
                        section.setBlockState(x, SectionPos.sectionRelative(y), z, block, false);
                    }
                }
            }
        }
    }

    private int transformElevationToY(final int elevation) {
        final int y = Mth.floor((elevation * heightScale) + configuration.heightOffset());
        return Mth.clamp(y, minY, maxY);
    }

    @Override
    public int getSeaLevel() {
        return configuration.heightOffset();
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types heightmap, final LevelHeightAccessor levelHeight, final RandomState randomState) {
        final int surfaceY = sampleBaseSurfaceY(x, z, randomState);
        if (heightmap.isOpaque().test(fluidBlock)) {
            return Math.max(surfaceY, getSeaLevel()) + 1;
        }
        return surfaceY + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor levelHeight, final RandomState randomState) {
        final int minY = levelHeight.getMinBuildHeight();
        final int surfaceY = sampleBaseSurfaceY(x, z, randomState);
        final int topY = Math.max(surfaceY, getSeaLevel());
        final BlockState[] blocks = new BlockState[topY - minY];
        for (int i = 0; i < blocks.length; i++) {
            final int y = i + minY;
            blocks[i] = y > surfaceY ? fluidBlock : fillBlock;
        }
        return new NoiseColumn(minY, blocks);
    }

    private int sampleBaseSurfaceY(final int x, final int z, final RandomState randomState) {
        final GeoProvider geoProvider = GeoProviderHolder.get(randomState);
        if (geoProvider != null) {
            final ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
            final ShortRaster elevation = geoProvider.getOrLoadSync(chunkPos).get(EarthAttachments.ELEVATION);
            if (elevation != null) {
                return transformElevationToY(elevation.getInt(SectionPos.sectionRelative(x), SectionPos.sectionRelative(z)));
            }
        }
        return minY;
    }

    @Override
    public void addDebugScreenInfo(final List<String> lines, final RandomState randomState, final BlockPos pos) {
    }

    @Override
    public ChunkGeneratorStructureState createState(final HolderLookup<StructureSet> structureSetLookup, final RandomState randomState, final long seed) {
        final HolderLookup<StructureSet> filteredLookup = new HolderLookup<>() {
            @Override
            public Optional<Holder.Reference<StructureSet>> get(final ResourceKey<StructureSet> key) {
                return structureSetLookup.get(key).filter(set -> !shouldDropStructureSet(set.value()));
            }

            @Override
            public Optional<HolderSet.Named<StructureSet>> get(final TagKey<StructureSet> tag) {
                return structureSetLookup.get(tag);
            }

            @Override
            public Stream<Holder.Reference<StructureSet>> listElements() {
                return structureSetLookup.listElements().filter(set -> !shouldDropStructureSet(set.value()));
            }

            @Override
            public Stream<HolderSet.Named<StructureSet>> listTags() {
                return structureSetLookup.listTags();
            }
        };
        return ChunkGeneratorStructureState.createForNormal(randomState, seed, biomeSource, filteredLookup);
    }

    // TODO: The Stronghold's placement doesn't make too much sense (and the biome scan is too expensive) - should be replaced with something else
    private static boolean shouldDropStructureSet(final StructureSet set) {
        return set.placement() instanceof ConcentricRingsStructurePlacement;
    }
}
