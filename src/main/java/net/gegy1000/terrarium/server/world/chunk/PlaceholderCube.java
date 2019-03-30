package net.gegy1000.terrarium.server.world.chunk;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.gegy1000.terrarium.server.world.chunk.tracker.HookedChunkMarker;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PlaceholderCube extends Cube implements HookedChunkMarker {
    public PlaceholderCube(World world, CubePos pos) {
        super(new PlaceholderChunk(world, pos.getX(), pos.getZ()), pos.getY());
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
