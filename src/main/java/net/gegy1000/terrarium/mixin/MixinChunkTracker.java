package net.gegy1000.terrarium.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.gegy1000.terrarium.api.ChunkTracker;
import net.minecraft.class_3898;
import net.minecraft.server.world.ServerChunkManagerEntry;
import net.minecraft.world.ChunkSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(class_3898.class)
public class MixinChunkTracker implements ChunkTracker {
    @Shadow(aliases = "field_17220")
    private Long2ObjectLinkedOpenHashMap<ServerChunkManagerEntry> entries;
    @Shadow(aliases = "field_17227")
    private ChunkSaveHandler saveHandler;

    @Override
    public Collection<ServerChunkManagerEntry> getTrackedEntries() {
        return this.entries.values();
    }

    @Override
    public ChunkSaveHandler getSaveHandler() {
        return this.saveHandler;
    }
}
