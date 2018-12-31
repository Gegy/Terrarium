package net.gegy1000.terrarium.server.util;

import java.util.Iterator;

public class SpiralIterator implements Iterator<Point2i> {
    private final int range;

    private int segment;
    private int layer = 1;
    private Point2i point = new Point2i(0, 0);

    public SpiralIterator(int range) {
        this.range = range;
    }

    public static Iterable<Point2i> of(int range) {
        return () -> new SpiralIterator(range);
    }

    @Override
    public boolean hasNext() {
        return this.layer < this.range;
    }

    @Override
    public Point2i next() {
        Point2i point = this.point;
        switch (this.segment) {
            case 0:
                point.x++;
                if (Math.abs(point.x) >= this.layer) {
                    this.segment++;
                }
                break;
            case 1:
                point.y++;
                if (Math.abs(point.y) >= this.layer) {
                    this.segment++;
                }
                break;
            case 2:
                point.x--;
                if (Math.abs(point.x) >= this.layer) {
                    this.segment++;
                }
                break;
            case 3:
                point.y--;
                if (Math.abs(point.y) >= this.layer) {
                    this.segment = 0;
                    this.layer++;
                }
                break;
        }
        return point;
    }
}
