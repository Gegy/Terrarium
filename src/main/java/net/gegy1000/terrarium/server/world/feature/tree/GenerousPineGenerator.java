package net.gegy1000.terrarium.server.world.feature.tree;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.gen.feature.WorldGenTaiga1;

public class GenerousPineGenerator extends WorldGenTaiga1 {
    public GenerousPineGenerator() {
        super();
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
