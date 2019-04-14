package net.gegy1000.terrarium.server.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class ReplaceRandomLayer extends GenLayer {
    private final int replace;
    private final int replacement;
    private final int chance;

    public ReplaceRandomLayer(int replace, int replacement, int chance, long seed, GenLayer parent) {
        super(seed);
        this.replace = replace;
        this.replacement = replacement;
        this.chance = chance;
        this.parent = parent;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] parent = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] result = IntCache.getIntCache(areaWidth * areaHeight);
        for (int z = 0; z < areaHeight; z++) {
            for (int x = 0; x < areaWidth; x++) {
                int index = x + z * areaWidth;
                int sample = parent[index];
                result[index] = this.shouldReplace(areaX + x, areaY + z, sample) ? this.replacement : sample;
            }
        }
        return result;
    }

    private boolean shouldReplace(long x, long z, int sample) {
        if (sample == this.replace) {
            this.initChunkSeed(x, z);
            return this.nextInt(this.chance) == 0;
        }
        return false;
    }
}
