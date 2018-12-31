package net.gegy1000.terrarium.mixin;

import net.gegy1000.terrarium.api.ChunkTracker;
import net.minecraft.class_3898;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerChunkManagerEntry;
import net.minecraft.world.ChunkSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager implements ChunkTracker {
    @Shadow(aliases = "field_17254")
    private class_3898 tracker;

    @Override
    public Collection<ServerChunkManagerEntry> getTrackedEntries() {
        return ((ChunkTracker) this.tracker).getTrackedEntries();
    }

    @Override
    public ChunkSaveHandler getSaveHandler() {
        return ((ChunkTracker) this.tracker).getSaveHandler();
    }
}
