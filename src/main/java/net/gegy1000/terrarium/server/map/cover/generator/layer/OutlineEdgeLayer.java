package net.gegy1000.terrarium.server.map.cover.generator.layer;

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

        this.outlineHorizontal(areaWidth, areaHeight, sampleWidth, parent, result);
        this.outlineVertical(areaWidth, areaHeight, sampleWidth, parent, result);

        return result;
    }

    private void outlineHorizontal(int areaWidth, int areaHeight, int sampleWidth, int[] parent, int[] result) {
        int last;
        for (int z = 0; z < areaHeight; z++) {
            last = -1;
            for (int x = 0; x < areaWidth; x++) {
                int parentX = x + 1;
                int parentZ = z + 1;
                int sample = parent[parentX + parentZ * sampleWidth];
                if (last != sample && last != -1) {
                    result[x + z * areaWidth] = this.outline;
                } else {
                    result[x + z * areaWidth] = sample;
                }
                last = sample;
            }
        }
    }

    private void outlineVertical(int areaWidth, int areaHeight, int sampleWidth, int[] parent, int[] result) {
        int last;
        for (int x = 0; x < areaWidth; x++) {
            last = -1;
            for (int z = 0; z < areaHeight; z++) {
                int index = x + z * areaWidth;
                int parentX = x + 1;
                int parentZ = z + 1;
                int sample = parent[parentX + parentZ * sampleWidth];
                if (result[index] != this.outline) {
                    if (last != sample && last != -1) {
                        result[index] = this.outline;
                    } else {
                        result[index] = sample;
                    }
                }
                last = sample;
            }
        }
    }
}
