package net.gegy1000.terrarium.server.world.cover;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class CoverSettings extends Biome.Settings {
    private static final Biome PARENT = Biomes.PLAINS;

    public CoverSettings() {
        this.precipitation(PARENT.getPrecipitation());
        this.category(PARENT.getCategory());
        this.depth(PARENT.getDepth()).scale(PARENT.getScale());
        this.temperature(PARENT.getTemperature()).downfall(PARENT.getRainfall());
        this.waterColor(PARENT.getWaterColor());
        this.waterFogColor(PARENT.getWaterFogColor());
    }
}
