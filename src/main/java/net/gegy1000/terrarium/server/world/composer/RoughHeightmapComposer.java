package net.gegy1000.terrarium.server.world.composer;

import net.daporkchop.fp2.mode.heightmap.HeightmapPos;
import net.daporkchop.fp2.mode.heightmap.HeightmapTile;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataKey;

import java.util.Collection;

public interface RoughHeightmapComposer {
    void compose(TerrariumWorld terrarium, ColumnData data, HeightmapPos pos, HeightmapTile tile);

    Collection<DataKey<?>> getRequiredData();
}
