package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.chunk.ComposableBiomeProvider;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGeneratorRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
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
    public IChunkGenerator getChunkGenerator(World world, String settingsString) {
        return new ComposableChunkGenerator(world);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        if (!world.isRemote) {
            return new ComposableBiomeProvider(world);
        }
        return new BiomeProviderSingle(Biomes.DEFAULT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld parent) {
        mc.displayGuiScreen(new TerrariumCustomizationGui(parent, TerrariumGeneratorRegistry.EARTH, TerrariumPresetRegistry.EARTH_DEFAULT));
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        TerrariumWorldData worldData = this.getWorldData(world);
        return worldData.getSettings().getProperties().getInteger("height_origin") < 40;
    }

    @Override
    public boolean isCustomizable() {
        return true;
    }

    private TerrariumWorldData getWorldData(World world) {
        TerrariumWorldData worldData = world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (worldData == null) {
            throw new IllegalStateException("Terrarium world capability not present");
        }
        return worldData;
    }
}
