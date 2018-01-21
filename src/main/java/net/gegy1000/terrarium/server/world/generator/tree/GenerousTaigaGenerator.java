package net.gegy1000.terrarium.server.world.generator.tree;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.gen.feature.WorldGenTaiga2;

public class GenerousTaigaGenerator extends WorldGenTaiga2 {
    public GenerousTaigaGenerator(boolean notify) {
        super(notify);
    }

    @Override
    protected boolean canGrowInto(Block blockType) {
        if (super.canGrowInto(blockType)) {
            return true;
        }
        Material material = blockType.getDefaultState().getMaterial();
        return material == Material.PLANTS || material == Material.VINE;
    }
}
