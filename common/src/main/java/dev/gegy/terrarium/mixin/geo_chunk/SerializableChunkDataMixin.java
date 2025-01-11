package dev.gegy.terrarium.mixin.geo_chunk;

import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.world.chunk.GeoChunkHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializableChunkData.class)
public class SerializableChunkDataMixin implements GeoChunkHolder {
    @Unique
    private static final String GEO_TAG_KEY = "geo";

    @Unique
    private GeoChunk terrarium$geoChunk = GeoChunk.EMPTY;

    @Inject(method = "copyOf", at = @At("RETURN"))
    private static void copyOf(final ServerLevel level, final ChunkAccess chunk, final CallbackInfoReturnable<SerializableChunkData> ci) {
        final SerializableChunkData data = ci.getReturnValue();
        GeoChunkHolder.put(data, GeoChunkHolder.get(chunk));
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private static void parse(final LevelHeightAccessor levelHeight, final RegistryAccess registries, final CompoundTag rootTag, final CallbackInfoReturnable<SerializableChunkData> ci) {
        final SerializableChunkData data = ci.getReturnValue();
        rootTag.getCompound(Terrarium.ID)
                .flatMap(terrariumRoot -> terrariumRoot.read(GEO_TAG_KEY, GeoChunk.CODEC))
                .ifPresent(geoChunk -> GeoChunkHolder.put(data, geoChunk));
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void write(final CallbackInfoReturnable<CompoundTag> ci) {
        final CompoundTag rootTag = ci.getReturnValue();
        if (!terrarium$geoChunk.isEmpty()) {
            final CompoundTag terrariumRoot = new CompoundTag();
            terrariumRoot.store(GEO_TAG_KEY, GeoChunk.CODEC, terrarium$geoChunk);
            rootTag.put(Terrarium.ID, terrariumRoot);
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void read(final ServerLevel level, final PoiManager poiManager, final RegionStorageInfo regionStorageInfo, final ChunkPos pos, final CallbackInfoReturnable<ProtoChunk> ci) {
        final ProtoChunk chunk = ci.getReturnValue();
        if (chunk instanceof ImposterProtoChunk) {
            return;
        }
        GeoChunkHolder.put(chunk, terrarium$geoChunk);
    }

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
