package net.gegy1000.terrarium.server.world.cover;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public class TerrariumCover {
    public static final Biome NONE = new None();

    private static class None extends Biome {
        None() {
            super(new Biome.Settings()
                    .configureSurfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.GRASS_CONFIG)
                    .precipitation(Biome.Precipitation.RAIN).category(Biome.Category.PLAINS)
                    .depth(0.125F).scale(0.05F)
                    .temperature(0.8F).downfall(0.4F)
                    .waterColor(0x3F76E4).waterFogColor(0x50533)
            );
        }
    }
}
