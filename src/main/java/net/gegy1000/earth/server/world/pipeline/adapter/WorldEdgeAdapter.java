/*
package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.terrarium.server.world.pipeline.adapter.ColumnAdapter;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.util.math.BlockPos;

// TODO
// The Earth *IS* flat!
public class WorldEdgeAdapter implements ColumnAdapter {
    private static final Cover EDGE_COVER = Cover.PERMANENT_SNOW;
    private static final int EDGE_HEIGHT = 70;

    private final DataKey<ShortRaster> heightKey;
    private final DataKey<ObjRaster<Cover>> coverKey;
    private final int oceanHeight;
    private final BlockPos min;
    private final BlockPos max;

    public WorldEdgeAdapter(DataKey<ShortRaster> heightKey, DataKey<ObjRaster<Cover>> coverKey, int oceanHeight, BlockPos min, BlockPos max) {
        this.heightKey = heightKey;
        this.coverKey = coverKey;
        this.oceanHeight = oceanHeight;
        this.min = new BlockPos(
                Math.min(min.getX(), max.getX()),
                Math.min(min.getY(), max.getY()),
                Math.min(min.getZ(), max.getZ())
        );
        this.max = new BlockPos(
                Math.max(min.getX(), max.getX()),
                Math.max(min.getY(), max.getY()),
                Math.max(min.getZ(), max.getZ())
        );
    }

    @Override
    public void apply(ColumnData data, int x, int z, int width, int height) {
        if (!this.isEdge(x, z) && !this.isEdge(x + width, z + height)) {
            return;
        }

        ShortRaster heightTile = data.get(this.heightKey);
        ObjRaster<Cover> coverTile = data.get(this.coverKey);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                if (this.isEdge(localX + x, localZ + z)) {
                    heightTile.set(localX, localZ, (short) (this.oceanHeight + EDGE_HEIGHT));
                    coverTile.set(localX, localZ, EDGE_COVER);
                }
            }
        }
    }

    private boolean isEdge(int x, int z) {
        return x < this.min.getX() || z < this.min.getZ() || x > this.max.getX() || z > this.max.getZ();
    }
}
*/
