package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.HeightmapStore;
import net.gegy1000.gengen.api.Heightmap;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.ColumnDataEntry;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Optional;

public class EarthChunkGenerator extends ComposableChunkGenerator {
    public EarthChunkGenerator(World world) {
        super(world);
    }

    @Override
    public void generateColumn(Chunk column) {
        HeightmapStore heightmapStore = column.getCapability(TerrariumEarth.heightmapCap(), null);
        if (heightmapStore == null) return;

        Heightmap heightmap = this.genereateHeightmap(column.getPos());
        if (heightmap != null) {
            heightmapStore.set(heightmap);
        }
    }

    private Heightmap genereateHeightmap(ChunkPos columnPos) {
        return this.terrarium.get().map(terrarium -> {
            ColumnDataCache dataCache = terrarium.getDataCache();

            try (ColumnDataEntry.Handle handle = dataCache.acquireEntry(columnPos)) {
                ColumnData data = handle.join();

                Optional<ShortRaster> heightOption = data.get(EarthDataKeys.HEIGHT);
                if (heightOption.isPresent()) {
                    ShortRaster heightRaster = heightOption.get();
                    return Heightmap.create(heightRaster::get);
                }
            }

            return null;
        }).orElse(null);
    }
}
