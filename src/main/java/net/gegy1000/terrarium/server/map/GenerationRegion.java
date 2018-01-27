package net.gegy1000.terrarium.server.map;

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
        this.minX = pos.getMinX() - GenerationRegion.BUFFER;
        this.minZ = pos.getMinZ() - GenerationRegion.BUFFER;
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
}
