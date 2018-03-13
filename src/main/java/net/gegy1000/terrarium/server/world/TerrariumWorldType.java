package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.chunk.ComposableBiomeProvider;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGeneratorRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TerrariumWorldType extends WorldType {
    private final ResourceLocation generatorIdentifier;
    private final ResourceLocation presetIdentifier;

    public TerrariumWorldType(String name, ResourceLocation generatorIdentifier, ResourceLocation presetIdentifier) {
        super(Terrarium.MODID + "." + name);
        this.generatorIdentifier = generatorIdentifier;
        this.presetIdentifier = presetIdentifier;
    }

    @Override
    public final IChunkGenerator getChunkGenerator(World world, String settingsString) {
        return new ComposableChunkGenerator(world);
    }

    @Override
    public final BiomeProvider getBiomeProvider(World world) {
        if (!world.isRemote) {
            return new ComposableBiomeProvider(world);
        }
        return new BiomeProviderSingle(Biomes.DEFAULT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final void onCustomizeButton(Minecraft mc, GuiCreateWorld parent) {
        TerrariumGenerator generator = TerrariumGeneratorRegistry.get(this.generatorIdentifier);
        if (generator == null) {
            Terrarium.LOGGER.warn("Found no generator with id {} for world type {}", this.generatorIdentifier, this.getName());
            return;
        }

        TerrariumPreset preset = TerrariumPresetRegistry.get(this.presetIdentifier);
        if (preset == null) {
            Terrarium.LOGGER.warn("Found no preset with id {} for world type {}", this.presetIdentifier, this.getName());
            return;
        }

        mc.displayGuiScreen(new TerrariumCustomizationGui(parent, this, generator, preset));
    }

    @Override
    public final boolean isCustomizable() {
        return true;
    }

    public TerrariumWorldData getWorldData(World world) {
        TerrariumWorldData worldData = world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (worldData == null) {
            throw new IllegalStateException("Terrarium world capability not present");
        }
        return worldData;
    }

    public boolean isHidden() {
        return false;
    }
}
