package dev.gegy.terrarium.world.chunk;

import dev.gegy.terrarium.backend.GeoChunk;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface GeoChunkHolder {
    static void put(final ChunkAccess chunk, final GeoChunk geoChunk) {
        if (chunk instanceof final GeoChunkHolder holder) {
            holder.terrarium$putGeoChunk(geoChunk);
        }
    }

    static GeoChunk get(final ChunkAccess chunk) {
        if (chunk instanceof final GeoChunkHolder holder) {
            return Objects.requireNonNullElse(holder.terrarium$getGeoChunk(), GeoChunk.EMPTY);
        }
        return GeoChunk.EMPTY;
    }

    void terrarium$putGeoChunk(GeoChunk geoChunk);

    @Nullable
    GeoChunk terrarium$getGeoChunk();
}
