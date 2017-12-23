package net.gegy1000.terrarium.server.map;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.minecraft.util.math.MathHelper;

public class GenerationRegion {
    public static final int SIZE = 128;
    public static final int SAMPLE_SIZE = SIZE + 1;

    private final RegionTilePos pos;
    private final int minBlockX;
    private final int minBlockZ;
    private final int scaledSize;
    private final short[] heights;
    private final GlobType[] globcover;

    public GenerationRegion(RegionTilePos pos, Coordinate minimumCoordinate, int scaledSize, short[] heights, GlobType[] globcover) {
        this.pos = pos;
        this.minBlockX = MathHelper.floor(minimumCoordinate.getBlockX());
        this.minBlockZ = MathHelper.floor(minimumCoordinate.getBlockZ());
        this.scaledSize = scaledSize;
        this.heights = heights;
        this.globcover = globcover;
    }

    public RegionTilePos getPos() {
        return this.pos;
    }

    public int getHeight(int scaledX, int scaledZ) {
        int localX = scaledX - this.minBlockX;
        int localZ = scaledZ - this.minBlockZ;
        return this.heights[localX + localZ * this.scaledSize];
    }

    public GlobType getGlobType(int scaledX, int scaledZ) {
        int localX = scaledX - this.minBlockX;
        int localZ = scaledZ - this.minBlockZ;
        return this.globcover[localX + localZ * this.scaledSize];
    }
}
