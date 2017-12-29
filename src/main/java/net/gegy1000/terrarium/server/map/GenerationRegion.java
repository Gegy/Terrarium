package net.gegy1000.terrarium.server.map;

import net.gegy1000.terrarium.server.map.glob.GlobType;

public class GenerationRegion {
    public static final int SIZE = 512;

    private final RegionTilePos pos;
    private final int minBlockX;
    private final int minBlockZ;
    private final short[] heights;
    private final GlobType[] globcover;

    public GenerationRegion(RegionTilePos pos, RegionData data) {
        this.pos = pos;
        this.minBlockX = pos.getMinX();
        this.minBlockZ = pos.getMinZ();
        this.heights = data.getHeights();
        this.globcover = data.getGlobcover();
    }

    public RegionTilePos getPos() {
        return this.pos;
    }

    public int getHeight(int scaledX, int scaledZ) {
        int localX = scaledX - this.minBlockX;
        int localZ = scaledZ - this.minBlockZ;
        return this.heights[localX + localZ * GenerationRegion.SIZE];
    }

    public GlobType getGlobType(int scaledX, int scaledZ) {
        int localX = scaledX - this.minBlockX;
        int localZ = scaledZ - this.minBlockZ;
        return this.globcover[localX + localZ * GenerationRegion.SIZE];
    }
}
