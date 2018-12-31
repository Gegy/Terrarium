package net.gegy1000.terrarium.server.world.cover.generator.layer;

import net.minecraft.class_3630;
import net.minecraft.world.biome.layer.InitLayer;

public class SelectionSeedLayer implements InitLayer {
    private final int range;

    public SelectionSeedLayer(int range) {
        this.range = range;
    }

    @Override
    public int sample(class_3630 context, int x, int y) {
        return context.nextInt(this.range);
    }
}
