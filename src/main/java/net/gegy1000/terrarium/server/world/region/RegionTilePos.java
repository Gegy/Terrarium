package net.gegy1000.terrarium.server.world.region;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;

public class RegionTilePos {
    private final int tileX;
    private final int tileZ;

    public RegionTilePos(int tileX, int tileZ) {
        this.tileX = tileX;
        this.tileZ = tileZ;
    }

    public int getTileX() {
        return this.tileX;
    }

    public int getTileZ() {
        return this.tileZ;
    }

    public int getMinBufferedX() {
        return this.tileX * GenerationRegion.SIZE - GenerationRegion.BUFFER;
    }

    public int getMinBufferedZ() {
        return this.tileZ * GenerationRegion.SIZE - GenerationRegion.BUFFER;
    }

    public Coordinate getMinCoordinate() {
        return Coordinate.fromBlock(this.tileX * GenerationRegion.SIZE, this.tileZ * GenerationRegion.SIZE);
    }

    public Coordinate getMinBufferedCoordinate() {
        return this.getMinCoordinate().addBlock(-GenerationRegion.BUFFER, -GenerationRegion.BUFFER);
    }

    public Coordinate getMaxCoordinate() {
        return this.getMinCoordinate().addBlock(GenerationRegion.SIZE, GenerationRegion.SIZE);
    }

    public Coordinate getMaxBufferedCoordinate() {
        return this.getMinBufferedCoordinate().addBlock(GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);
    }

    @Override
    public String toString() {
        return "RegionTilePos{tileX=" + this.tileX + ", tileZ=" + this.tileZ + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RegionTilePos) {
            RegionTilePos pos = (RegionTilePos) obj;
            return pos.tileX == this.tileX && pos.tileZ == this.tileZ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.tileX | this.tileZ << 16;
    }
}
