package net.gegy1000.terrarium.client.preview;

import net.minecraft.world.biome.Biome;

public class PreviewColumnData {
    private final Biome[] biomes;

    public PreviewColumnData(Biome[] biomes) {
        this.biomes = biomes;
    }

    public Biome[] getBiomes() {
        return this.biomes;
    }
}
