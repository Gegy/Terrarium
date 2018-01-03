package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.CustomizeEarthGui;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.EarthBiomeProvider;
import net.gegy1000.terrarium.server.world.generator.EarthChunkGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class EarthWorldType extends WorldType {
    public EarthWorldType() {
        super(Terrarium.MODID + ".earth");
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String settings) {
        return new EarthChunkGenerator(world, world.getSeed(), settings, false);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        if (world.isRemote) {
            return new BiomeProviderSingle(Biomes.DEFAULT);
        }
        return new EarthBiomeProvider(world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld parent) {
        mc.displayGuiScreen(new CustomizeEarthGui(parent));
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        TerrariumWorldData worldData = world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        return worldData != null && worldData.getGenerationHandler().getSettings().heightOffset >= 40;
    }

    @Override
    public boolean isCustomizable() {
        return true;
    }
}
