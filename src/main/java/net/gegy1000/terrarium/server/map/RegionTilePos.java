package net.gegy1000.terrarium.server.map;

import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

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

    public int getMinX() {
        return this.tileX * GenerationRegion.SIZE;
    }

    public int getMinZ() {
        return this.tileZ * GenerationRegion.SIZE;
    }

    public Coordinate getMinCoordinate(EarthGenerationSettings settings) {
        return Coordinate.fromBlock(settings, this.tileX * GenerationRegion.SIZE, this.tileZ * GenerationRegion.SIZE);
    }

    public Coordinate getMinBufferedCoordinate(EarthGenerationSettings settings) {
        return this.getMinCoordinate(settings).addBlock(-GenerationRegion.BUFFER, -GenerationRegion.BUFFER);
    }

    public Coordinate getMaxCoordinate(EarthGenerationSettings settings) {
        return this.getMinCoordinate(settings).addBlock(GenerationRegion.SIZE, GenerationRegion.SIZE);
    }

    public Coordinate getMaxBufferedCoordinate(EarthGenerationSettings settings) {
        return this.getMinBufferedCoordinate(settings).addBlock(GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);
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
        return (this.tileX * 31 + this.tileX) * 31;
    }
}
