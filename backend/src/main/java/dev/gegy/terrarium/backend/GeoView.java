package dev.gegy.terrarium.backend;

public record GeoView(int x0, int z0, int x1, int z1) {
    public int width() {
        return x1 - x0 + 1;
    }

    public int height() {
        return z1 - z0 + 1;
    }
}
