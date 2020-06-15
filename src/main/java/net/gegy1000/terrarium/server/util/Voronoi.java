package net.gegy1000.terrarium.server.util;

import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public final class Voronoi {
    private static final int FUZZ_SIZE = 64;
    private static final int FUZZ_MASK = FUZZ_SIZE - 1;

    private final float[] fuzzTable;

    public Voronoi(float fuzzRadius, long seed) {
        Random random = new Random(seed);
        this.fuzzTable = makeFuzzTable(random, fuzzRadius);
    }

    private static float[] makeFuzzTable(Random random, float radius) {
        float[] table = new float[FUZZ_SIZE * FUZZ_SIZE * 2];

        for (int y = 0; y < FUZZ_SIZE; y++) {
            for (int x = 0; x < FUZZ_SIZE; x++) {
                float fx = fuzz(random, radius);
                float fy = fuzz(random, radius);

                int idx = (x + y * FUZZ_SIZE) * 2;
                table[idx] = fx;
                table[idx + 1] = fy;
            }
        }

        return table;
    }

    private static float fuzz(Random random, float fuzzRange) {
        float offset = 2 * random.nextFloat() - 1;
        return 0.5F + offset * fuzzRange;
    }

    public void scaleBytes(byte[] src, byte[] dst, DataView srcView, DataView dstView,
                           float scaleX, float scaleY, float offsetX, float offsetY
    ) {
        int dstWidth = dstView.getWidth();
        int dstHeight = dstView.getHeight();

        int srcWidth = srcView.getWidth();
        int srcHeight = srcView.getHeight();

        if (dstWidth <= srcWidth && dstHeight <= srcHeight) {
            // nearest-neighbor sampling
            for (int dstY = 0; dstY < dstHeight; dstY++) {
                int srcY = MathHelper.floor(dstY * scaleY + offsetY);
                for (int dstX = 0; dstX < dstWidth; dstX++) {
                    int srcX = MathHelper.floor(dstX * scaleX + offsetX);
                    dst[dstX + dstY * dstWidth] = src[srcX + srcY * srcWidth];
                }
            }

            return;
        }

        for (int y = 0; y < dstHeight; y++) {
            float srcY = y * scaleY + offsetY;

            for (int x = 0; x < dstWidth; x++) {
                float srcX = x * scaleX + offsetX;

                int srcIndex = this.getCellIndex(srcView, srcX, srcY);
                int dstIndex = x + y * dstWidth;

                dst[dstIndex] = src[srcIndex];
            }
        }
    }

    private int getCellIndex(DataView srcView, float x, float y) {
        int originX = MathHelper.floor(x);
        int originY = MathHelper.floor(y);

        int offsetX = srcView.getX();
        int offsetY = srcView.getY();

        int minX = Math.max(originX - 1, 0);
        int minY = Math.max(originY - 1, 0);

        int maxX = Math.min(originX + 1, srcView.getWidth() - 1);
        int maxY = Math.min(originY + 1, srcView.getHeight() - 1);

        int cellIndex = 0;
        float selectionDistance = Float.MAX_VALUE;

        for (int srcY = minY; srcY <= maxY; srcY++) {
            for (int srcX = minX; srcX <= maxX; srcX++) {
                int tx = (srcX + offsetX) & FUZZ_MASK;
                int ty = (srcY + offsetY) & FUZZ_MASK;
                int ti = tx + ty * FUZZ_SIZE;

                float fuzzX = this.fuzzTable[ti];
                float fuzzY = this.fuzzTable[ti + 1];

                float deltaX = x - (srcX + fuzzX);
                float deltaY = y - (srcY + fuzzY);
                float distance = deltaX * deltaX + deltaY * deltaY;

                if (distance < selectionDistance) {
                    selectionDistance = distance;
                    cellIndex = srcX + srcY * srcView.getWidth();
                }
            }
        }

        return cellIndex;
    }
}
