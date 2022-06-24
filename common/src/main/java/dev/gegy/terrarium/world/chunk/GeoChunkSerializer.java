package dev.gegy.terrarium.world.chunk;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import dev.gegy.terrarium.backend.GeoChunk;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.slf4j.Logger;

public class GeoChunkSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String GEO_TAG_KEY = "geo";

    public static void read(final ProtoChunk chunk, final CompoundTag root) {
        if (root.contains(GEO_TAG_KEY)) {
            final DataResult<GeoChunk> parseResult = GeoChunk.CODEC.parse(NbtOps.INSTANCE, root.get(GEO_TAG_KEY));
            parseResult.result().ifPresent(geoChunk -> GeoChunkHolder.put(chunk, geoChunk));
            parseResult.error().ifPresent(error -> LOGGER.error("Failed to parse GeoChunk: {}", error));
        }
    }

    public static boolean write(final ChunkAccess chunk, final CompoundTag root) {
        final GeoChunk geoChunk = GeoChunkHolder.get(chunk);
        if (!geoChunk.isEmpty()) {
            root.put(GEO_TAG_KEY, Util.getOrThrow(GeoChunk.CODEC.encodeStart(NbtOps.INSTANCE, geoChunk), IllegalStateException::new));
            return true;
        }
        return false;
    }
}
