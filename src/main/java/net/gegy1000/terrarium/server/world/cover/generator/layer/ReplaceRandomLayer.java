package net.gegy1000.terrarium.server.world.cover.generator.layer;

import net.minecraft.class_3630;
import net.minecraft.class_3661;

public class ReplaceRandomLayer implements class_3661 {
    private final int replace;
    private final int replacement;
    private final int chance;

    public ReplaceRandomLayer(int replace, int replacement, int chance) {
        this.replace = replace;
        this.replacement = replacement;
        this.chance = chance;
    }

    @Override
    public int method_15866(class_3630 context, int value) {
        if (value == this.replace && context.nextInt(this.chance) == 0) {
            return this.replacement;
        }
        return value;
    }
}
