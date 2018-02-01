package net.gegy1000.terrarium.server.map.cover.generator.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class ConnectHorizontalLayer extends GenLayer {
    private final int connect;

    public ConnectHorizontalLayer(int connect, long seed, GenLayer parent) {
        super(seed);
        this.connect = connect;
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

        for (int z = 0; z < areaHeight; z++) {
            for (int x = 0; x < areaWidth; x++) {
                int parentX = x + 1;
                int parentZ = z + 1;
                int sample = parent[parentX + parentZ * sampleWidth];
                int type = sample;
                if (sample != this.connect) {
                    type = this.connect(sampleWidth, parent, parentX, parentZ, type);
                }
                result[x + z * areaWidth] = type;
            }
        }

        return result;
    }

    private int connect(int sampleWidth, int[] parent, int parentX, int parentZ, int type) {
        boolean east = parent[(parentX + 1) + parentZ * sampleWidth] == this.connect;
        boolean west = parent[(parentX - 1) + parentZ * sampleWidth] == this.connect;
        boolean south = parent[parentX + (parentZ + 1) * sampleWidth] == this.connect;
        if (east && south) {
            if (parent[(parentX + 1) + (parentZ + 1) * sampleWidth] != this.connect) {
                type = this.connect;
            }
        } else if (west && south) {
            if (parent[(parentX - 1) + (parentZ + 1) * sampleWidth] != this.connect) {
                type = this.connect;
            }
        }
        return type;
    }
}
