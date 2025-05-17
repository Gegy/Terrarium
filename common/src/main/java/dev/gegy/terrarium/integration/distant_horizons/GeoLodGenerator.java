package dev.gegy.terrarium.integration.distant_horizons;

import com.mojang.logging.LogUtils;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.worldGeneration.EDhApiDistantGeneratorMode;
import com.seibel.distanthorizons.api.enums.worldGeneration.EDhApiWorldGeneratorReturnType;
import com.seibel.distanthorizons.api.interfaces.block.IDhApiBiomeWrapper;
import com.seibel.distanthorizons.api.interfaces.block.IDhApiBlockStateWrapper;
import com.seibel.distanthorizons.api.interfaces.override.worldGenerator.IDhApiWorldGenerator;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.objects.data.DhApiTerrainDataPoint;
import com.seibel.distanthorizons.api.objects.data.IDhApiFullDataSource;
import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.raster.RasterShape;
import dev.gegy.terrarium.world.GeoProvider;
import dev.gegy.terrarium.world.generator.biome.GeoBiomeSource;
import dev.gegy.terrarium.world.generator.chunk.GeoChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public final class GeoLodGenerator implements IDhApiWorldGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final IDhApiLevelWrapper levelWrapper;
    private final GeoProvider geoProvider;
    private final GeoChunkGenerator generator;
    private final GeoBiomeSource biomeSource;

    private final ThreadLocal<WrapperCache> wrapperCache;

    public GeoLodGenerator(final IDhApiLevelWrapper levelWrapper, final GeoProvider geoProvider, final GeoChunkGenerator generator) {
        this.levelWrapper = levelWrapper;
        this.geoProvider = geoProvider;
        this.generator = generator;
        biomeSource = generator.getBiomeSource();
        wrapperCache = ThreadLocal.withInitial(() -> new WrapperCache(levelWrapper));
    }

    @Override
    public void preGeneratorTaskStart() {
    }

    @Override
    public byte getLargestDataDetailLevel() {
        return 24;
    }

    @Override
    public CompletableFuture<Void> generateLod(final int chunkPosMinX, final int chunkPosMinZ, final int lodPosX, final int lodPosZ, final byte detailLevel, final IDhApiFullDataSource pooledFullDataSource, final EDhApiDistantGeneratorMode generatorMode, final ExecutorService worldGeneratorThreadPool, final Consumer<IDhApiFullDataSource> resultConsumer) {
        final int lodSizePoints = pooledFullDataSource.getWidthInDataColumns();
        final int lodSizeBlocks = lodSizePoints * (1 << detailLevel);

        final int x0 = SectionPos.sectionToBlockCoord(chunkPosMinX);
        final int z0 = SectionPos.sectionToBlockCoord(chunkPosMinZ);
        final int x1 = x0 + lodSizeBlocks - 1;
        final int z1 = z0 + lodSizeBlocks - 1;
        final GeoView blockSampleView = new GeoView(x0, z0, x1, z1);

        final RasterShape outputShape = new RasterShape(lodSizePoints, lodSizePoints);

        return geoProvider.load(blockSampleView, outputShape).thenAcceptAsync(
                geoChunk -> {
                    buildLod(pooledFullDataSource, geoChunk);
                    resultConsumer.accept(pooledFullDataSource);
                },
                worldGeneratorThreadPool
        );
    }

    private void buildLod(final IDhApiFullDataSource output, final GeoChunk geoChunk) {
        final WrapperCache wrappers = wrapperCache.get();

        final int minY = levelWrapper.getMinHeight();
        final int maxY = minY + levelWrapper.getMaxHeight();
        final int absoluteTop = maxY - minY;

        final GeoBiomeSource.FlatChunkResolver biomeResolver = biomeSource.chunkResolver(geoChunk);

        generator.buildLod(new GeoChunkGenerator.LodOutput() {
            private final List<DhApiTerrainDataPoint> columnDataPoints = new ArrayList<>();
            private int columnX;
            private int columnZ;
            @Nullable
            private IDhApiBiomeWrapper columnBiome;
            private int lastLayerTop;

            @Override
            public void beginColumn(final int x, final int z, final Holder<Biome> biome) {
                columnX = x;
                columnZ = z;
                columnBiome = wrappers.getBiome(biome);
                lastLayerTop = 0;
            }

            @Override
            public void addLayerUpTo(final int inclusiveTopY, final BlockState blockState) {
                final int layerTop = Mth.clamp(inclusiveTopY - minY + 1, 0, absoluteTop);
                if (layerTop == lastLayerTop) {
                    return;
                }
                final IDhApiBlockStateWrapper block = wrappers.getBlockState(blockState);
                final IDhApiBiomeWrapper biome = Objects.requireNonNull(columnBiome);
                columnDataPoints.add(DhApiTerrainDataPoint.create((byte) 0, 0, 0, lastLayerTop, layerTop, block, biome));
                lastLayerTop = layerTop;
            }

            @Override
            public void endColumn() {
                if (lastLayerTop < absoluteTop) {
                    final IDhApiBiomeWrapper biome = Objects.requireNonNull(columnBiome);
                    columnDataPoints.add(DhApiTerrainDataPoint.create((byte) 0, 0, 0, lastLayerTop, absoluteTop, wrappers.airBlock(), biome));
                }

                output.setApiDataPointColumn(columnX, columnZ, columnDataPoints);
                columnDataPoints.clear();
            }
        }, geoChunk, biomeResolver);
    }

    @Override
    public EDhApiWorldGeneratorReturnType getReturnType() {
        return EDhApiWorldGeneratorReturnType.API_DATA_SOURCES;
    }

    @Override
    public boolean runApiValidation() {
        return Terrarium.isDevelopmentEnvironment();
    }

    @Override
    public void close() {
    }

    private static class WrapperCache {
        private final IDhApiLevelWrapper levelWrapper;

        private final IDhApiBlockStateWrapper airBlock;
        @Nullable
        private final IDhApiBiomeWrapper defaultBiome;

        private final Map<BlockState, IDhApiBlockStateWrapper> blockStates = new IdentityHashMap<>();
        private final Map<Holder<Biome>, IDhApiBiomeWrapper> biomes = new HashMap<>();

        private WrapperCache(final IDhApiLevelWrapper levelWrapper) {
            this.levelWrapper = levelWrapper;
            airBlock = DhApi.Delayed.wrapperFactory.getAirBlockStateWrapper();
            defaultBiome = lookupBiomeById(Biomes.THE_VOID);
        }

        public IDhApiBlockStateWrapper airBlock() {
            return airBlock;
        }

        public IDhApiBlockStateWrapper getBlockState(final BlockState blockState) {
            return blockStates.computeIfAbsent(blockState, this::lookupBlockState);
        }

        private IDhApiBlockStateWrapper lookupBlockState(final BlockState blockState) {
            try {
                return DhApi.Delayed.wrapperFactory.getBlockStateWrapper(new BlockState[]{blockState}, levelWrapper);
            } catch (final ClassCastException e) {
                throw new IllegalStateException(e);
            }
        }

        public IDhApiBiomeWrapper getBiome(final Holder<Biome> biome) {
            return biomes.computeIfAbsent(biome, this::lookupBiome);
        }

        private IDhApiBiomeWrapper lookupBiome(final Holder<Biome> biome) {
            final IDhApiBiomeWrapper result = biome.unwrapKey().map(this::lookupBiomeById).orElse(null);
            if (result != null) {
                return result;
            }
            return Objects.requireNonNull(defaultBiome, "No default biome available");
        }

        @Nullable
        private IDhApiBiomeWrapper lookupBiomeById(final ResourceKey<Biome> biome) {
            try {
                return DhApi.Delayed.wrapperFactory.getBiomeWrapper(biome.location().toString(), levelWrapper);
            } catch (final IOException ignored) {
                LOGGER.warn("Could not find biome with id {}, will not use for LODs", biome.location());
                return null;
            }
        }
    }
}
