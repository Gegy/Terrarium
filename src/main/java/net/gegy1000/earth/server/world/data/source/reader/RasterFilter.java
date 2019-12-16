package net.gegy1000.earth.server.world.data.source.reader;

import net.gegy1000.terrarium.server.world.data.raster.IntegerRaster;
import net.gegy1000.terrarium.server.world.data.raster.Raster;

public enum RasterFilter {
    NONE {
        @Override
        public int apply(int x, int a, int b, int c) {
            return x;
        }

        @Override
        public <T extends IntegerRaster<?>> void apply(T input, T output) {
            Raster.rasterCopy(input, output);
        }
    },
    LEFT {
        @Override
        public int apply(int x, int a, int b, int c) {
            return x + a;
        }
    },
    UP {
        @Override
        public int apply(int x, int a, int b, int c) {
            return x + b;
        }
    },
    AVERAGE {
        @Override
        public int apply(int x, int a, int b, int c) {
            return x + (a + b) / 2;
        }
    },
    PAETH {
        @Override
        public int apply(int x, int a, int b, int c) {
            int p = a + b - c;
            int deltaA = Math.abs(a - p);
            int deltaB = Math.abs(b - p);
            int deltaC = Math.abs(c - p);
            if (deltaA < deltaB && deltaA < deltaC) {
                return x + a;
            } else if (deltaB < deltaC) {
                return x + b;
            } else {
                return x + c;
            }
        }
    };

    public abstract int apply(int x, int a, int b, int c);

    public <T extends IntegerRaster<?>> void apply(T input, T output) {
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int value = input.getInt(x, y);
                int a = x > 0 ? output.getInt(x - 1, y) : 0;
                int b = y > 0 ? output.getInt(x, y - 1) : 0;
                int c = x > 0 && y > 0 ? output.getInt(x - 1, y - 1) : 0;
                output.setInt(x, y, this.apply(value, a, b, c));
            }
        }
    }

    public static RasterFilter byId(int id) {
        switch (id) {
            case 1: return LEFT;
            case 2: return UP;
            case 3: return AVERAGE;
            case 4: return PAETH;
            default: return NONE;
        }
    }
}
