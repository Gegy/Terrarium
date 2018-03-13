package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Random;

public class EarthWorldType extends TerrariumWorldType {
    private static final ResourceLocation GENERATOR = new ResourceLocation(TerrariumEarth.MODID, "earth_generator");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.MODID, "earth_default");

    public EarthWorldType() {
        super("earth", GENERATOR, PRESET);
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        TerrariumWorldData worldData = this.getWorldData(world);
        return worldData.getSettings().getProperties().getInteger("height_origin") < 40;
    }
}
