package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.BlockPos;

// The Earth *IS* flat!
public class WorldEdgeAdapter implements RegionAdapter {
    private static final CoverType<?> EDGE_COVER = EarthCoverTypes.SNOW;
    private static final int EDGE_HEIGHT = 70;

    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<CoverRasterTile> coverComponent;
    private final int oceanHeight;
    private final BlockPos min;
    private final BlockPos max;

    public WorldEdgeAdapter(RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<CoverRasterTile> coverComponent, int oceanHeight, BlockPos min, BlockPos max) {
        this.heightComponent = heightComponent;
        this.coverComponent = coverComponent;
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
    public void adapt(RegionData data, int x, int z, int width, int height) {
        if (!this.isEdge(x, z) && !this.isEdge(x + width, z + height)) {
            return;
        }

        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
        CoverRasterTile coverTile = data.getOrExcept(this.coverComponent);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                if (this.isEdge(localX + x, localZ + z)) {
                    heightTile.setShort(localX, localZ, (short) (this.oceanHeight + EDGE_HEIGHT));
                    coverTile.set(localX, localZ, EDGE_COVER);
                }
            }
        }
    }

    private boolean isEdge(int x, int z) {
        return x < this.min.getX() || z < this.min.getZ() || x > this.max.getX() || z > this.max.getZ();
    }
}
