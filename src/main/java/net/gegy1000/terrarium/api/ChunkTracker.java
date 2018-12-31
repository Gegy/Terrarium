package net.gegy1000.terrarium.api;

import net.minecraft.server.world.ServerChunkManagerEntry;
import net.minecraft.world.ChunkSaveHandler;

import java.util.Collection;

public interface ChunkTracker {
    Collection<ServerChunkManagerEntry> getTrackedEntries();

    ChunkSaveHandler getSaveHandler();
}
