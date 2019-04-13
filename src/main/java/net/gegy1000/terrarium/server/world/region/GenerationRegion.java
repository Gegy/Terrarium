package net.gegy1000.terrarium.server.world.region;

import com.google.common.base.Preconditions;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.RasterData;

public class GenerationRegion {
    public static final int BUFFER = 16;

    public static final int SIZE = 256;
    public static final int BUFFERED_SIZE = SIZE + BUFFER * 2;

    private final RegionTilePos pos;
    private final RegionData data;
    private final int minX;
    private final int minZ;

    public GenerationRegion(RegionTilePos pos, RegionData data) {
        this.pos = pos;
        this.data = data;
        this.minX = pos.getMinBufferedX();
        this.minZ = pos.getMinBufferedZ();
    }

    public RegionTilePos getPos() {
        return this.pos;
    }

    public int getMinX() {
        return this.minX;
    }

    public int getMinZ() {
        return this.minZ;
    }

    public RegionData getData() {
        return this.data;
    }

    public <T extends RasterData<V>, V> V sample(RegionComponentType<T> componentType, int x, int z) {
        T raster = this.data.get(componentType);
        Preconditions.checkNotNull(raster, "raster not present");
        return raster.get(x & 0xFF, z & 0xFF);
    }
}
