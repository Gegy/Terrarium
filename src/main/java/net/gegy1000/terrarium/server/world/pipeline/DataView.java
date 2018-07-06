package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;

public class DataView {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public DataView(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
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
        return Coordinate.fromBlock(this.x, this.y);
    }

    public Coordinate getMaxCoordinate() {
        return Coordinate.fromBlock(this.x + this.width, this.y + this.height);
    }

    public DataView grow(int lowerX, int lowerY, int upperX, int upperY) {
        return new DataView(this.x - lowerX, this.y - lowerY, this.width + upperX, this.height + upperY);
    }

    public boolean contains(DataView view) {
        return view.getX() >= this.x && view.getY() >= this.y
                && view.getMaxX() <= this.getMaxX() && view.getMaxY() <= this.getMaxY();
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
}
