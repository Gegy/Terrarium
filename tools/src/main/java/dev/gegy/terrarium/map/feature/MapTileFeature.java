package dev.gegy.terrarium.map.feature;

import com.mojang.logging.LogUtils;
import dev.gegy.terrarium.Mapper;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.loader.Cacher;
import dev.gegy.terrarium.backend.loader.ConcurrencyLimiter;
import dev.gegy.terrarium.backend.loader.FileCacher;
import dev.gegy.terrarium.backend.loader.GuavaInMemoryCacher;
import dev.gegy.terrarium.backend.loader.HttpLoader;
import dev.gegy.terrarium.backend.loader.Loader;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MapTileFeature implements MapFeature {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Loader<Tile, BufferedImage> loader;

    public MapTileFeature(final HttpClient httpClient, final ConcurrencyLimiter limiter, final Path cachePath, final Function<Tile, String> urlGetter) {
        final Loader<byte[], BufferedImage> imageLoader = key -> CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.ofNullable(ImageIO.read(new ByteArrayInputStream(key)));
            } catch (final IOException e) {
                LOGGER.error("Failed to load tile {}", key, e);
                return Optional.empty();
            }
        }, Mapper.EXECUTOR);
        final Loader<Tile, byte[]> httpLoader = new HttpLoader(httpClient, REQUEST_TIMEOUT).mapKey(tile -> URI.create(urlGetter.apply(tile)));
        final Cacher<Tile, byte[]> fileCacher = new FileCacher(Mapper.EXECUTOR).mapKey(tile -> cachePath.resolve(tile.zoom + "/" + tile.x + "/" + tile.y + ".png"));
        final Cacher<Tile, BufferedImage> inMemoryCacher = new GuavaInMemoryCacher<>(Duration.ofMinutes(1), 256);
        loader = imageLoader.compose(limiter.wrap(httpLoader).cached(fileCacher))
                .cached(inMemoryCacher);
    }

    @Override
    public CompletableFuture<Optional<BufferedImage>> render(final EarthLayers layers, final int tileX, final int tileY, final int zoomLevel, final int x0, final int y0, final int x1, final int y1) {
        return loader.load(new Tile(tileX, tileY, zoomLevel));
    }

    public record Tile(int x, int y, int zoom) {
    }
}
