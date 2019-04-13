package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraftforge.common.BiomeDictionary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class BiomeClassification {
    private static final int POOL_SIZE = 16;
    private static final LinkedList<BiomeClassification> POOL = new LinkedList<>();

    private final List<BiomeDictionary.Type> include = new ArrayList<>();
    private final List<BiomeDictionary.Type> exclude = new ArrayList<>();

    public static BiomeClassification take() {
        if (POOL.isEmpty()) {
            return new BiomeClassification();
        }
        return POOL.removeLast();
    }

    private static void returnToPool(BiomeClassification classification) {
        POOL.addLast(classification);

        if (POOL.size() > POOL_SIZE * 2) {
            TerrariumEarth.LOGGER.warn("Biome classification pool has grown excessively!");
            while (POOL.size() > POOL_SIZE) {
                POOL.removeLast();
            }
        }
    }

    void include(BiomeDictionary.Type type) {
        this.include.add(type);
        this.exclude.remove(type);
    }

    void exclude(BiomeDictionary.Type type) {
        this.exclude.add(type);
        this.include.remove(type);
    }

    List<BiomeDictionary.Type> getInclude() {
        return this.include;
    }

    List<BiomeDictionary.Type> getExclude() {
        return this.exclude;
    }

    public void release() {
        this.include.clear();
        this.exclude.clear();
        returnToPool(this);
    }

    @Override
    public String toString() {
        return "BiomeClassification{" + this.include + "}";
    }

    public void classifyRainfall(short rainfall) {
        this.exclude(BiomeDictionary.Type.WET);
        this.exclude(BiomeDictionary.Type.DRY);

        if (rainfall < 250) {
            this.include(BiomeDictionary.Type.DRY);
        } else if (rainfall > 1500) {
            this.include(BiomeDictionary.Type.WET);
        }
    }

    public void classifyTemperature(float temperature) {
        this.exclude(BiomeDictionary.Type.COLD);
        this.exclude(BiomeDictionary.Type.HOT);

        if (temperature < 12.0F) {
            if (temperature < 0.0F) {
                this.include(BiomeDictionary.Type.SNOWY);
            }
            this.include(BiomeDictionary.Type.COLD);
        } else if (temperature > 22.0F) {
            this.include(BiomeDictionary.Type.HOT);
        }
    }
}
