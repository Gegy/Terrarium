package net.gegy1000.terrarium.server.world.region;

import net.gegy1000.terrarium.api.RegionSaveHandler;
import net.minecraft.world.ChunkSaveHandler;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;

import javax.annotation.Nullable;
import java.io.File;

public class ChunkSaveStateChecker {
    @Nullable
    public static synchronized RegionFile getRegionFileIfExists(File worldFile, int chunkX, int chunkZ) {
        File regionFolder = new File(worldFile, "region");
        File regionFile = new File(regionFolder, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
        if (regionFile.exists()) {
            return RegionFileCache.getRegionFile(worldFile, chunkX, chunkZ);
        }
        return null;
    }

    public static boolean isChunkSaved(ChunkSaveHandler saveHandler, int chunkX, int chunkZ) {
        if (saveHandler instanceof RegionSaveHandler) {
            File saveRoot = ((RegionSaveHandler) saveHandler).getSaveRoot();
            RegionFile regionFile = getRegionFileIfExists(saveRoot, chunkX, chunkZ);
            if (regionFile != null) {
                return regionFile.hasChunk(chunkX & 31, chunkZ & 31);
            }
        }
        return false;
    }
}
