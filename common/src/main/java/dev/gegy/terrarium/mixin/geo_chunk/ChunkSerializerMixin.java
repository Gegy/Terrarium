package dev.gegy.terrarium.mixin.geo_chunk;

import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.world.chunk.GeoChunkSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
    @Inject(method = "read", at = @At("RETURN"))
    private static void read(final ServerLevel level, final PoiManager pois, final ChunkPos pos, final CompoundTag rootTag, final CallbackInfoReturnable<ProtoChunk> ci) {
        final ProtoChunk chunk = ci.getReturnValue();
        if (chunk instanceof ImposterProtoChunk) {
            return;
        }

        if (rootTag.contains(Terrarium.ID, Tag.TAG_COMPOUND)) {
            final CompoundTag terrariumRoot = rootTag.getCompound(Terrarium.ID);
            GeoChunkSerializer.read(chunk, terrariumRoot);
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private static void write(final ServerLevel level, final ChunkAccess chunk, final CallbackInfoReturnable<CompoundTag> ci) {
        final CompoundTag rootTag = ci.getReturnValue();

        final CompoundTag terrariumRoot = new CompoundTag();
        if (GeoChunkSerializer.write(chunk, terrariumRoot)) {
            rootTag.put(Terrarium.ID, terrariumRoot);
        }
    }
}
