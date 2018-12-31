package net.gegy1000.terrarium.server.world.surface;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;

public enum VoidSurfaceConfig implements SurfaceConfig {
    INSTANCE;

    @Override
    public BlockState getTopMaterial() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public BlockState getUnderMaterial() {
        return Blocks.AIR.getDefaultState();
    }

    public static VoidSurfaceConfig deserialize(Dynamic<?> dynamic) {
        return VoidSurfaceConfig.INSTANCE;
    }
}
