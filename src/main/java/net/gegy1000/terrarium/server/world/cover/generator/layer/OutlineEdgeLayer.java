package net.gegy1000.terrarium.server.world.cover.generator.layer;

import net.minecraft.class_3630;
import net.minecraft.class_3663;

public class OutlineEdgeLayer implements class_3663 {
    private final int outlineValue;

    public OutlineEdgeLayer(int outlineValue) {
        this.outlineValue = outlineValue;
    }

    @Override
    public int sample(class_3630 context, int up, int right, int down, int left, int self) {
        if (self != up || self != right || self != down || self != left) {
            return this.outlineValue;
        }
        return self;
    }
}
