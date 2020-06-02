package net.gegy1000.terrarium.server.world.data.source;

import net.gegy1000.terrarium.Terrarium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public final class TerrariumCacheDirs {
    public static final Path LEGACY_ROOT = Paths.get(".", "mods/terrarium/cache");
    public static final Path GLOBAL_ROOT = Paths.get(".", "mods/terrarium/cache2");

    static {
        try {
            if (Files.exists(LEGACY_ROOT)) {
                Terrarium.LOGGER.info("Deleting legacy terrarium cache directory");

                try (Stream<Path> walk = Files.walk(LEGACY_ROOT)) {
                    Stream<Path> stream = walk.sorted(Comparator.reverseOrder());
                    for (Path child : (Iterable<Path>) stream::iterator) {
                        Files.delete(child);
                    }
                }
            }
        } catch (IOException e) {
            Terrarium.LOGGER.warn("Failed to delete legacy terrarium cache", e);
        }

        try {
            Files.createDirectories(GLOBAL_ROOT);
        } catch (IOException e) {
            Terrarium.LOGGER.warn("Failed to create cache directories", e);
        }
    }
}
