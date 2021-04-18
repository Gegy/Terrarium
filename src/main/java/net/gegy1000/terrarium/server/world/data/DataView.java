package net.gegy1000.terrarium.server.world.data;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.util.math.ChunkPos;

public final class DataView {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private DataView(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static DataView rect(int width, int height) {
        return new DataView(0, 0, width, height);
    }

    public static DataView rect(int x, int y, int width, int height) {
        return new DataView(x, y, width, height);
    }

    public static DataView square(int x, int y, int size) {
        return new DataView(x, y, size, size);
    }

    public static DataView of(ChunkPos columnPos) {
        return new DataView(columnPos.getXStart(), columnPos.getZStart(), 16, 16);
    }

    public static DataView fromCorners(int minX, int minY, int maxX, int maxY) {
        return new DataView(minX, minY, maxX - minX, maxY - minY);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getMinX() {
        return this.x;
    }

    public int getMinY() {
        return this.y;
    }

    public int getMaxX() {
        return this.x + this.width;
    }

    public int getMaxY() {
        return this.y + this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Coordinate getMinCoordinate() {
        return Coordinate.atBlock(this.x, this.y);
    }

    public Coordinate getMaxCoordinate() {
        return Coordinate.atBlock(this.x + this.width, this.y + this.height);
    }

    public DataView grow(int lowerX, int lowerY, int upperX, int upperY) {
        return new DataView(this.x - lowerX, this.y - lowerY, this.width + upperX + lowerX, this.height + upperY + lowerY);
    }

    public DataView grow(int amount) {
        return new DataView(this.x - amount, this.y - amount, this.width + amount * 2, this.height + amount * 2);
    }

    public DataView offset(int x, int y) {
        return new DataView(this.x + x, this.y + y, this.width, this.height);
    }

    public boolean contains(int x, int y) {
        return x >= this.x && y >= this.y && x < this.getMaxX() && y < this.getMaxY();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataView) {
            DataView dataView = (DataView) obj;
            return this.x == dataView.x && this.y == dataView.y && this.width == dataView.height && this.height == dataView.height;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.y;
        result = 31 * result + this.width;
        result = 31 * result + this.height;
        return result;
    }

    @Override
    public String toString() {
        return "DataView{" + "x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" + this.height + '}';
    }
}
