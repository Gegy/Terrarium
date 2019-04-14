package net.gegy1000.terrarium.server.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class OutlineEdgeLayer extends GenLayer {
    private final int outline;

    public OutlineEdgeLayer(int outline, long seed, GenLayer parent) {
        super(seed);
        this.outline = outline;
        this.parent = parent;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int sampleX = areaX - 1;
        int sampleZ = areaY - 1;
        int sampleWidth = areaWidth + 2;
        int sampleHeight = areaHeight + 2;
        int[] parent = this.parent.getInts(sampleX, sampleZ, sampleWidth, sampleHeight);

        int[] result = IntCache.getIntCache(areaWidth * areaHeight);

        this.outlineHorizontal(areaWidth, areaHeight, sampleWidth, sampleHeight, parent, result);
        this.outlineVertical(areaWidth, areaHeight, sampleWidth, sampleHeight, parent, result);

        return result;
    }

    private void outlineHorizontal(int areaWidth, int areaHeight, int sampleWidth, int sampleHeight, int[] parentBuffer, int[] buffer) {
        for (int sampleZ = 0; sampleZ < sampleHeight; sampleZ++) {
            int localZ = sampleZ - 1;
            int lastType = -1;

            for (int sampleX = 0; sampleX < sampleWidth; sampleX++) {
                int localX = sampleX - 1;

                int sampleType = parentBuffer[sampleX + sampleZ * sampleWidth];

                if (localX >= 0 && localZ >= 0 && localX < areaWidth && localZ < areaHeight) {
                    int index = localX + localZ * areaWidth;
                    if (lastType != sampleType && lastType != -1) {
                        buffer[index] = this.outline;
                    } else {
                        buffer[index] = sampleType;
                    }
                }

                lastType = sampleType;
            }
        }
    }

    private void outlineVertical(int areaWidth, int areaHeight, int sampleWidth, int sampleHeight, int[] parentBuffer, int[] buffer) {
        for (int sampleX = 0; sampleX < sampleWidth; sampleX++) {
            int localX = sampleX - 1;
            int lastType = -1;

            for (int sampleZ = 0; sampleZ < sampleHeight; sampleZ++) {
                int localZ = sampleZ - 1;

                int sampleType = parentBuffer[sampleX + sampleZ * sampleWidth];

                if (localX >= 0 && localZ >= 0 && localX < areaWidth && localZ < areaHeight) {
                    int index = localX + localZ * areaWidth;
                    if (lastType != sampleType && lastType != -1) {
                        buffer[index] = this.outline;
                    }
                }

                lastType = sampleType;
            }
        }
    }
}
