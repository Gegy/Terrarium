package net.gegy1000.earth.server.world.ecology;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public interface SoilPredicate {
    SoilPredicate ANY = (world, soilPos, soilState) -> {
        Block soilBlock = soilState.getBlock();
        return soilBlock == Blocks.GRASS || soilBlock == Blocks.DIRT || soilBlock == Blocks.FARMLAND
                || soilBlock == Blocks.SAND || soilBlock == Blocks.CLAY;
    };

    static SoilPredicate plantable(IPlantable plantable) {
        return (world, soilPos, soilState) -> {
            Block soilBlock = soilState.getBlock();
            return soilBlock.canSustainPlant(soilState, world, soilPos, EnumFacing.UP, plantable);
        };
    }

    boolean canGrowOn(World world, BlockPos soilPos, IBlockState soilState);
}
