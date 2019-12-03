package net.gegy1000.earth.server.world.data.source.cache;

import com.google.common.base.Preconditions;
import cubicchunks.regionlib.api.region.IRegionProvider;
import cubicchunks.regionlib.api.region.key.IKey;
import cubicchunks.regionlib.api.region.key.IKeyProvider;
import cubicchunks.regionlib.lib.provider.SharedCachedRegionProvider;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import net.gegy1000.terrarium.Terrarium;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public final class RegionTileCache<T extends IKey<T>> implements TileCache<T> {
    private final IRegionProvider<T> regionProvider;

    private RegionTileCache(IRegionProvider<T> regionProvider) {
        this.regionProvider = regionProvider;
    }

    public static <T extends IKey<T>> Builder<T> builder() {
        return new Builder<>();
    }

    @Nullable
    @Override
    public InputStream in(T key) throws IOException {
        return this.regionProvider.fromExistingRegion(key, region -> {
            return region.readValue(key).map(b -> new ByteArrayInputStream(b.array()));
        }).flatMap(Function.identity()).orElse(null);
    }

    @Nullable
    @Override
    public OutputStream out(T key) throws IOException {
        return this.regionProvider.fromRegion(key, region -> {
            return new ByteArrayOutputStream(4096) {
                @Override
                public void close() throws IOException {
                    region.writeValue(key, ByteBuffer.wrap(this.toByteArray()));
                }
            };
        });
    }

    @Override
    public void delete(T key) throws IOException {
        this.regionProvider.forRegion(key, region -> {
            region.writeValue(key, null);
        });
    }

    public static class Builder<T extends IKey<T>> {
        private IKeyProvider<T> keyProvider;

        private Path directory;
        private int sectorSize = 4096;

        Builder() {
        }

        public Builder<T> keyProvider(IKeyProvider<T> keyProvider) {
            this.keyProvider = keyProvider;
            return this;
        }

        public Builder<T> inDirectory(Path path) {
            this.directory = path;
            return this;
        }

        public Builder<T> sectorSize(int sectorSize) {
            this.sectorSize = sectorSize;
            return this;
        }

        public RegionTileCache<T> build() {
            Preconditions.checkNotNull(this.keyProvider, "key provider must be set");
            Preconditions.checkNotNull(this.directory, "cache directory must be set");

            try {
                Files.createDirectories(this.directory);
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to create cache directory", e);
            }

            // TODO: should the regionprovider be cached? and when should clear?
            IRegionProvider<T> regionProvider = SimpleRegionProvider.createDefault(this.keyProvider, this.directory, this.sectorSize);
            regionProvider = new SharedCachedRegionProvider<>(regionProvider);

            return new RegionTileCache<>(regionProvider);
        }
    }
}
