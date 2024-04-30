package dev.gegy.terrarium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public record ColorRamp(
        float[] xs,
        float[] reds,
        float[] greens,
        float[] blues,
        float[] redGrads,
        float[] greenGrads,
        float[] blueGrads
) {
    public static Builder builder() {
        return new Builder();
    }

    public int get(final float x) {
        final int index = indexAt(x);
        if (index <= 0) {
            return pack(reds[0], greens[0], blues[0]);
        } else if (index >= xs.length) {
            final int lastIndex = xs.length - 1;
            return pack(reds[lastIndex], greens[lastIndex], blues[lastIndex]);
        }
        final float a = x - xs[index];
        return pack(
                reds[index] + a * redGrads[index - 1],
                greens[index] + a * greenGrads[index - 1],
                blues[index] + a * blueGrads[index - 1]
        );
    }

    private int pack(final float red, final float green, final float blue) {
        return (int) red << 16 | (int) green << 8 | (int) blue;
    }

    private int indexAt(final float x) {
        final int i = Arrays.binarySearch(xs, x);
        return i >= 0 ? i : -i - 1;
    }

    public static class Builder {
        private final List<Point> points = new ArrayList<>();

        private Builder() {
        }

        public Builder color(final float x, final int red, final int green, final int blue) {
            points.add(new Point(x, red, green, blue));
            return this;
        }

        public Builder color(final float x, final int color) {
            final int red = color >> 16 & 0xff;
            final int green = color >> 8 & 0xff;
            final int blue = color & 0xff;
            return color(x, red, green, blue);
        }

        public ColorRamp build() {
            if (points.size() < 2) {
                throw new IllegalStateException("Cannot have less than 2 points");
            }

            final float[] xs = new float[points.size()];
            final float[] reds = new float[points.size()];
            final float[] greens = new float[points.size()];
            final float[] blues = new float[points.size()];
            final float[] redGrads = new float[points.size() - 1];
            final float[] greenGrads = new float[points.size() - 1];
            final float[] blueGrads = new float[points.size() - 1];

            points.sort(Comparator.comparingDouble(Point::x));

            for (int i = 0; i < points.size(); i++) {
                final Point point = points.get(i);
                xs[i] = point.x();
                reds[i] = point.red();
                greens[i] = point.green();
                blues[i] = point.blue();
            }

            for (int i = 0; i < points.size() - 1; i++) {
                final Point start = points.get(i);
                final Point end = points.get(i + 1);
                final float deltaX = end.x() - start.x();
                redGrads[i] = (end.red() - start.red()) / deltaX;
                greenGrads[i] = (end.green() - start.green()) / deltaX;
                blueGrads[i] = (end.blue() - start.blue()) / deltaX;
            }

            return new ColorRamp(xs, reds, greens, blues, redGrads, greenGrads, blueGrads);
        }
    }

    private record Point(float x, float red, float green, float blue) {
    }
}