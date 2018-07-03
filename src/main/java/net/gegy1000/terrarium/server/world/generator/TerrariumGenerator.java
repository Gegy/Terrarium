package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TerrariumGenerator extends ICapabilityProvider {
    ChunkCompositionProcedure getCompositionProcedure();

    Coordinate getSpawnPosition();

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return false;
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }
}
