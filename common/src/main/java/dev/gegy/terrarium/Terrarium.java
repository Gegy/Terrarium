package dev.gegy.terrarium;

import dev.gegy.terrarium.backend.earth.EarthTiles;
import dev.gegy.terrarium.backend.loader.ConcurrencyLimiter;
import dev.gegy.terrarium.backend.tile.TileCache;
import dev.gegy.terrarium.world.generator.chunk.TerrariumChunkGenerators;
import dev.gegy.terrarium.world.generator.chunk.data.GeoChunkLoader;
import net.minecraft.Util;

import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

public class Terrarium {
    public static final String ID = "terrarium";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .executor(Util.ioPool())
            .build();

    @Nullable
    private static EarthTiles.Config tiles;

    public static void bootstrap(final Path terrariumDirectory) {
        tiles = new EarthTiles.Config(
                HTTP_CLIENT,
                new ConcurrencyLimiter(16),
                terrariumDirectory.resolve("cache2"),
                Util.backgroundExecutor(),
                Util.ioPool()
        );

        TerrariumChunkGenerators.bootstrap();
        GeoChunkLoader.bootstrap();
    }

    public static EarthTiles createTiles(final TileCache cache) {
        final EarthTiles.Config tiles = Objects.requireNonNull(Terrarium.tiles, "Terrarium was not bootstrapped");
        return tiles.create(cache);
    }
}
