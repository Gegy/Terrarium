package net.gegy1000.terrarium.server.world.data;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.util.math.ChunkPos;

public class DataView {
    private final int minX;
    private final int minY;
    private final int width;
    private final int height;

    private DataView(int minX, int minY, int width, int height) {
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
    }

    public static DataView of(int width, int height) {
        return new DataView(0, 0, width, height);
    }

    public static DataView of(int x, int y, int width, int height) {
        return new DataView(x, y, width, height);
    }

    public static DataView ofSquare(int x, int y, int size) {
        return new DataView(x, y, size, size);
    }

    public static DataView of(ChunkPos columnPos) {
        return new DataView(columnPos.getXStart(), columnPos.getZStart(), 16, 16);
    }

    public static DataView ofCorners(int minX, int minY, int maxX, int maxY) {
        return new DataView(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public int minX() {
        return this.minX;
    }

    public int minY() {
        return this.minY;
    }

    public int maxX() {
        return this.minX + this.width - 1;
    }

    public int maxY() {
        return this.minY + this.height - 1;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public Coordinate minCoordinate() {
        return Coordinate.atBlock(this.minX(), this.minY());
    }

    public Coordinate maxCoordinate() {
        return Coordinate.atBlock(this.maxX(), this.maxY());
    }

    public DataView grow(int lowerX, int lowerY, int upperX, int upperY) {
        return new DataView(this.minX - lowerX, this.minY - lowerY, this.width + upperX + lowerX, this.height + upperY + lowerY);
    }

    public DataView grow(int amount) {
        return new DataView(this.minX - amount, this.minY - amount, this.width + amount * 2, this.height + amount * 2);
    }

    public DataView offset(int x, int y) {
        return new DataView(this.minX + x, this.minY + y, this.width, this.height);
    }

    public boolean contains(DataView view) {
        return view.minX() >= this.minX() && view.minY() >= this.minY()
                && view.maxX() <= this.maxX() && view.maxY() <= this.maxY();
    }

    public boolean contains(int x, int y) {
        return x >= this.minX() && y >= this.minY() && x <= this.maxX() && y <= this.maxY();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataView) {
            DataView dataView = (DataView) obj;
            return this.minX == dataView.minX && this.minY == dataView.minY && this.width == dataView.height && this.height == dataView.height;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.minX;
        result = 31 * result + this.minY;
        result = 31 * result + this.width;
        result = 31 * result + this.height;
        return result;
    }

    @Override
    public String toString() {
        return "DataView{" + "x=" + this.minX + ", y=" + this.minY + ", width=" + this.width + ", height=" + this.height + '}';
    }
}
