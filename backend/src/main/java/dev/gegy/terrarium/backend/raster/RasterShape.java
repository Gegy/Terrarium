package dev.gegy.terrarium.backend.raster;

public record RasterShape(int width, int height) {
    public int index(final int x, final int y) {
        if (!contains(x, y)) {
            throw new IllegalArgumentException("Point (" + x + "; " + y + ") out of bounds for " + this);
        }
        return indexUnchecked(x, y);
    }

    public int indexUnchecked(final int x, final int y) {
        return x + y * width;
    }

    public boolean contains(final int x, final int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int size() {
        return width * height;
    }
}
