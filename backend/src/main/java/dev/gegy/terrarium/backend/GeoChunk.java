package dev.gegy.terrarium.backend;

import com.mojang.serialization.Codec;

public class GeoChunk {
    public static final GeoChunk EMPTY = new GeoChunk();
    public static final Codec<GeoChunk> CODEC = Codec.unit(EMPTY);

    public boolean isEmpty() {
        return true;
    }
}
