package net.gegy1000.terrarium.server.map;

import net.gegy1000.terrarium.server.map.cover.CoverType;

public class GenerationRegion {
    public static final int BUFFER = 16;

    public static final int SIZE = 512;
    public static final int BUFFERED_SIZE = SIZE + BUFFER * 2;

    private final RegionTilePos pos;
    private final int minBlockX;
    private final int minBlockZ;
    private final short[] heights;
    private final CoverType[] cover;

    public GenerationRegion(RegionTilePos pos, RegionData data) {
        this.pos = pos;
        this.minBlockX = pos.getMinX() - BUFFER;
        this.minBlockZ = pos.getMinZ() - BUFFER;
        this.heights = data.getHeights();
        this.cover = data.getCover();
    }

    public RegionTilePos getPos() {
        return this.pos;
    }

    public int getHeight(int blockX, int blockZ) {
        int localX = blockX - this.minBlockX;
        int localZ = blockZ - this.minBlockZ;
        return this.heights[localX + localZ * GenerationRegion.BUFFERED_SIZE];
    }

    public CoverType getCoverType(int blockX, int blockZ) {
        int localX = blockX - this.minBlockX;
        int localZ = blockZ - this.minBlockZ;
        return this.cover[localX + localZ * GenerationRegion.BUFFERED_SIZE];
    }
}
