package net.gegy1000.earth.server.util;

import java.util.stream.IntStream;

public final class ZoomLevels {
    public final int min;
    public final int max;

    private ZoomLevels(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public IntStream stream() {
        return IntStream.range(this.min, this.max + 1);
    }

    public boolean contains(int zoom) {
        return zoom >= this.min && zoom <= this.max;
    }

    public static ZoomLevels range(int minInclusive, int maxInclusive) {
        return new ZoomLevels(minInclusive, maxInclusive);
    }
}
