package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Random;

public class CoverDebugWorldType extends TerrariumWorldType {
    private static final ResourceLocation GENERATOR = new ResourceLocation(TerrariumEarth.MODID, "debug_generator");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.MODID, "debug_default");

    public CoverDebugWorldType() {
        super("earth_debug", GENERATOR, PRESET);
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        return true;
    }
}
