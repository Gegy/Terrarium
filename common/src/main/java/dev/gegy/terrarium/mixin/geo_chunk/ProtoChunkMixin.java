package dev.gegy.terrarium.mixin.geo_chunk;

import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.world.chunk.GeoChunkHolder;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ProtoChunk.class)
public class ProtoChunkMixin implements GeoChunkHolder {
    @Unique
    @Nullable
    private GeoChunk terrarium$geoChunk;

    @Override
    public void terrarium$putGeoChunk(final GeoChunk geoChunk) {
        terrarium$geoChunk = geoChunk;
    }

    @Override
    @Nullable
    public GeoChunk terrarium$getGeoChunk() {
        return terrarium$geoChunk;
    }
}
