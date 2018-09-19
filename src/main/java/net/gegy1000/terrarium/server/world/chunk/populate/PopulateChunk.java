package net.gegy1000.terrarium.server.world.chunk.populate;

import net.gegy1000.terrarium.server.world.chunk.ComposingChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface PopulateChunk extends ComposingChunk {
    World getGlobal();

    @Nullable
    BlockPos getSurface(int x, int z);
}
