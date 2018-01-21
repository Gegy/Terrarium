package net.gegy1000.terrarium.server.map.cover.generator.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class SelectionSeedLayer extends GenLayer {
    private final int range;

    public SelectionSeedLayer(int range, long seed) {
        super(seed);
        this.range = range;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] result = IntCache.getIntCache(areaWidth * areaHeight);
        for (int z = 0; z < areaHeight; z++) {
            for (int x = 0; x < areaWidth; x++) {
                this.initChunkSeed(areaX + x, areaY + z);
                result[x + z * areaWidth] = this.nextInt(this.range);
            }
        }
        return result;
    }
}
