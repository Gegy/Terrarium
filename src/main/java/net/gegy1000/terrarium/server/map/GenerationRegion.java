package net.gegy1000.terrarium.server.map;

import net.gegy1000.terrarium.server.map.glob.GlobType;

public class GenerationRegion {
    public static final int BUFFER = 16;

    public static final int SIZE = 512;
    public static final int BUFFERED_SIZE = SIZE + BUFFER * 2;

    private final RegionTilePos pos;
    private final int minBlockX;
    private final int minBlockZ;
    private final short[] heights;
    private final GlobType[] globcover;

    public GenerationRegion(RegionTilePos pos, RegionData data) {
        this.pos = pos;
        this.minBlockX = pos.getMinX() - BUFFER;
        this.minBlockZ = pos.getMinZ() - BUFFER;
        this.heights = data.getHeights();
        this.globcover = data.getGlobcover();
    }

    public RegionTilePos getPos() {
        return this.pos;
    }

    public int getHeight(int blockX, int blockZ) {
        int localX = blockX - this.minBlockX;
        int localZ = blockZ - this.minBlockZ;
        return this.heights[localX + localZ * GenerationRegion.BUFFERED_SIZE];
    }

    public GlobType getGlobType(int blockX, int blockZ) {
        int localX = blockX - this.minBlockX;
        int localZ = blockZ - this.minBlockZ;
        return this.globcover[localX + localZ * GenerationRegion.BUFFERED_SIZE];
    }
}
