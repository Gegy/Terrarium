package dev.gegy.terrarium.world.chunk;

import dev.gegy.terrarium.backend.GeoChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface GeoChunkHolder {
    static void put(final Object holder, final GeoChunk geoChunk) {
        if (holder instanceof final GeoChunkHolder h) {
            h.terrarium$putGeoChunk(geoChunk);
        }
    }

    static GeoChunk get(final Object holder) {
        if (holder instanceof final GeoChunkHolder h) {
            return Objects.requireNonNullElse(h.terrarium$getGeoChunk(), GeoChunk.EMPTY);
        }
        return GeoChunk.EMPTY;
    }

    void terrarium$putGeoChunk(GeoChunk geoChunk);

    @Nullable
    GeoChunk terrarium$getGeoChunk();
}
