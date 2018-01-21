package net.gegy1000.terrarium.server.map.cover.generator.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class SelectWeightedLayer extends GenLayer {
    private final Entry[] entries;
    private final int totalWeight;

    public SelectWeightedLayer(long seed, Entry... entries) {
        super(seed);
        this.entries = entries;

        int totalWeight = 0;
        for (Entry entry : entries) {
            totalWeight += entry.weight;
        }
        this.totalWeight = totalWeight;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] result = IntCache.getIntCache(areaWidth * areaHeight);
        for (int z = 0; z < areaHeight; z++) {
            for (int x = 0; x < areaWidth; x++) {
                this.initChunkSeed(areaX + x, areaY + z);
                result[x + z * areaWidth] = this.getSelection(this.nextInt(this.totalWeight));
            }
        }
        return result;
    }

    private int getSelection(int weight) {
        int currentWeight = 0;
        for (Entry entry : this.entries) {
            currentWeight += entry.weight;
            if (currentWeight >= weight) {
                return entry.value;
            }
        }
        return 0;
    }

    public static class Entry {
        private final int value;
        private final int weight;

        public Entry(int value, int weight) {
            this.value = value;
            this.weight = weight;
        }
    }
}
