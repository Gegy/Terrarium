package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.earth.climate.ClimateRasterSamplers;
import dev.gegy.terrarium.backend.earth.climate.ClimateRasters;
import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.earth.soil.SoilSuborder;
import dev.gegy.terrarium.backend.layer.LeveledRasterSampler;
import dev.gegy.terrarium.backend.layer.RasterSampler;
import dev.gegy.terrarium.backend.loader.Cacher;
import dev.gegy.terrarium.backend.loader.ConcurrencyLimiter;
import dev.gegy.terrarium.backend.loader.FileCacher;
import dev.gegy.terrarium.backend.loader.HttpLoader;
import dev.gegy.terrarium.backend.loader.Loader;
import dev.gegy.terrarium.backend.raster.EnumRaster;
import dev.gegy.terrarium.backend.raster.Raster;
import dev.gegy.terrarium.backend.raster.RasterShape;
import dev.gegy.terrarium.backend.raster.RasterType;
import dev.gegy.terrarium.backend.raster.ShortRaster;
import dev.gegy.terrarium.backend.raster.UnsignedByteRaster;
import dev.gegy.terrarium.backend.raster.reader.RasterFormat;
import dev.gegy.terrarium.backend.raster.reader.RasterReader;
import dev.gegy.terrarium.backend.tile.TileCache;
import dev.gegy.terrarium.backend.tile.TileKey;
import dev.gegy.terrarium.backend.tile.TileMap;
import dev.gegy.terrarium.backend.tile.TiledRasterSampler;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record EarthTiles(
        LeveledRasterSampler<ShortRaster> elevation,
        LeveledRasterSampler<EnumRaster<Cover>> landCover,
        LeveledRasterSampler<UnsignedByteRaster> cationExchangeCapacity,
        LeveledRasterSampler<ShortRaster> organicCarbonContent,
        LeveledRasterSampler<UnsignedByteRaster> soilPh,
        LeveledRasterSampler<UnsignedByteRaster> clayContent,
        LeveledRasterSampler<UnsignedByteRaster> siltContent,
        LeveledRasterSampler<UnsignedByteRaster> sandContent,
        LeveledRasterSampler<EnumRaster<SoilSuborder>> soilSuborder,
        ClimateRasterSamplers climateSamplers
) {
    private static final int TILE_SIZE = 1000;
    private static final RasterShape TILE_SHAPE = new RasterShape(TILE_SIZE, TILE_SIZE);
    private static final int ZOOM_BASE = 3;

    private static final String ENDPOINT = "https://terrarium.gegy.dev/geo3";
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);

    public static class Config {
        private final Loader<URI, byte[]> httpLoader;
        private final Path cacheRoot;
        private final Executor executor;
        private final Executor ioExecutor;

        public Config(final HttpClient httpClient, final ConcurrencyLimiter concurrencyLimiter, final Path cacheRoot, final Executor executor, final Executor ioExecutor) {
            httpLoader = concurrencyLimiter.wrap(new HttpLoader(httpClient, REQUEST_TIMEOUT));
            this.cacheRoot = cacheRoot;
            this.executor = executor;
            this.ioExecutor = ioExecutor;
        }

        public EarthTiles create(final TileCache cache) {
            return new EarthTiles(
                    elevation(cache),
                    landCover(cache),
                    soilUByteRaster("cec", cache),
                    organicCarbonContent(cache),
                    soilUByteRaster("ph", cache),
                    soilUByteRaster("clay", cache),
                    soilUByteRaster("silt", cache),
                    soilUByteRaster("sand", cache),
                    soilSuborder(cache),
                    climateSamplers()
            );
        }

        private LeveledRasterSampler<ShortRaster> elevation(final TileCache cache) {
            return createLeveledTiledRaster(cache, 0, 6, ShortRaster.TYPE, level -> {
                final Loader<TileKey, byte[]> fileLoader = httpLoader("elevation2", level)
                        .cached(fileCacher("elevation", level));
                return RasterReader.loader(RasterFormat.SHORT, executor)
                        .compose(fileLoader);
            });
        }

        private LeveledRasterSampler<EnumRaster<Cover>> landCover(final TileCache cache) {
            final RasterType<EnumRaster<Cover>> rasterType = EnumRaster.type(Cover.NONE, Cover.CODEC);
            return createLeveledTiledRaster(cache, 0, 4, rasterType, level -> {
                final Loader<TileKey, byte[]> fileLoader = httpLoader("landcover", level)
                        .cached(fileCacher("landcover", level));
                return RasterReader.loader(rasterType, Cover::byId, executor)
                        .compose(fileLoader);
            });
        }

        private LeveledRasterSampler<ShortRaster> organicCarbonContent(final TileCache cache) {
            return createLeveledTiledRaster(cache, 0, 4, ShortRaster.TYPE, level -> {
                final Loader<TileKey, byte[]> fileLoader = httpLoader("occ", level)
                        .cached(fileCacher("soil/" + "occ", level));
                return RasterReader.loader(RasterFormat.SHORT, executor)
                        .compose(fileLoader);
            });
        }

        private LeveledRasterSampler<UnsignedByteRaster> soilUByteRaster(final String name, final TileCache cache) {
            return createLeveledTiledRaster(cache, 0, 4, UnsignedByteRaster.TYPE, level -> {
                final Loader<TileKey, byte[]> fileLoader = httpLoader(name, level)
                        .cached(fileCacher("soil/" + name, level));
                final Loader<ShortRaster, UnsignedByteRaster> converter = Loader.from(UnsignedByteRaster::copyOf);
                return converter
                        .compose(RasterReader.loader(RasterFormat.SHORT, executor))
                        .compose(fileLoader);
            });
        }

        private LeveledRasterSampler<EnumRaster<SoilSuborder>> soilSuborder(final TileCache cache) {
            final RasterType<EnumRaster<SoilSuborder>> rasterType = EnumRaster.type(SoilSuborder.NONE, SoilSuborder.CODEC);
            return createLeveledTiledRaster(cache, 0, 4, rasterType, level -> {
                final Loader<TileKey, byte[]> fileLoader = httpLoader("usda", level)
                        .cached(fileCacher("soil/usda", level));
                return RasterReader.loader(rasterType, SoilSuborder::byId, executor)
                        .compose(fileLoader);
            });
        }

        private <V extends Raster> LeveledRasterSampler<V> createLeveledTiledRaster(final TileCache cache, final int minLevel, final int maxLevel, final RasterType<V> rasterType, final IntFunction<Loader<TileKey, V>> factory) {
            final List<RasterSampler<V>> levels = IntStream.rangeClosed(minLevel, maxLevel).mapToObj(level -> {
                final TileMap<V> map = createTileMap(level, factory.apply(level));
                final TileMap<V> cachedMap = map.cached(cache.createCacher(map));
                return new TiledRasterSampler<>(cachedMap, rasterType, executor);
            }).collect(Collectors.toList());
            return new LeveledRasterSampler<>(levels);
        }

        private <V extends Raster> TileMap<V> createTileMap(final int level, final Loader<TileKey, V> loader) {
            final int countY = (int) Math.floor(Math.pow(ZOOM_BASE, level));
            final int countX = countY * 2;
            return new TileMap<>(countX, countY, TILE_SHAPE, loader);
        }

        private Loader<TileKey, byte[]> httpLoader(final String route, final int level) {
            final String endpoint = ENDPOINT + "/" + route + "/" + level + "/";
            return httpLoader.mapKey(key -> URI.create(endpoint + key.path()));
        }

        private Cacher<TileKey, byte[]> fileCacher(final String name, final int level) {
            final Path sourceRoot = cacheRoot.resolve(name).resolve(String.valueOf(level));
            return new FileCacher(ioExecutor).mapKey(key -> sourceRoot.resolve(key.path()));
        }

        private ClimateRasterSamplers climateSamplers() {
            final Loader<Void, ClimateRasters> loader = ClimateRasters.loader(executor)
                    .compose(singleFileLoader("climatic_variables.xz"));
            return ClimateRasterSamplers.create(loader, executor);
        }

        private Loader<Void, byte[]> singleFileLoader(final String fileName) {
            return httpLoader.<Void>mapKey(v -> URI.create(ENDPOINT + "/" + fileName))
                    .cached(new FileCacher(ioExecutor).mapKey(v -> cacheRoot.resolve(fileName)));
        }
    }
}
