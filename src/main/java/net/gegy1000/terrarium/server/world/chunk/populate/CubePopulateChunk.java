package net.gegy1000.terrarium.server.world.chunk.populate;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class CubePopulateChunk implements PopulateChunk {
    private final World world;
    private final CubicPos pos;
    private final CubePos ccPos;

    public CubePopulateChunk(World world, CubicPos pos) {
        this.world = world;
        this.pos = pos;
        this.ccPos = this.pos.toCC();
    }

    @Override
    public CubicPos getPos() {
        return this.pos;
    }

    @Override
    public World getGlobal() {
        return this.world;
    }

    @Override
    @Nullable
    public BlockPos getSurface(int x, int z) {
        return ((ICubicWorld) this.world).getSurfaceForCube(this.ccPos, x, z, 0, ICubicWorld.SurfaceType.SOLID);
    }
}
