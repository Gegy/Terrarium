package net.gegy1000.terrarium.server.world;

import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.world.chunk.TerrariumChunkDelegate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Random;

public abstract class TerrariumWorldDefinition {
    private final String name;
    private final ResourceLocation identifier;
    private final ResourceLocation presetIdentifier;

    public TerrariumWorldDefinition(String name, ResourceLocation identifier, ResourceLocation presetIdentifier) {
        this.name = name;
        this.identifier = identifier;
        this.presetIdentifier = presetIdentifier;
    }

    public abstract TerrariumGeneratorInitializer createInitializer(World world, TerrariumChunkDelegate delegate, GenerationSettings settings);

    public abstract Collection<ICapabilityProvider> createCapabilities(World world, GenerationSettings settings);

    public abstract TerrariumCustomization buildCustomization();

    public String getName() {
        return this.name;
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    public ResourceLocation getPresetIdentifier() {
        return this.presetIdentifier;
    }

    public boolean isHidden() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(GuiCreateWorld parent, TerrariumWorldType worldType, TerrariumPreset preset) {
        return new EarthCustomizationGui(parent, worldType, preset);
    }

    public boolean shouldReduceSlimeSpawns(Random random, World world) {
        return false;
    }

    public TerrariumWorldType create() {
        // TODO: This crashes when mod is not present
        if (Loader.isModLoaded("cubicchunks")) {
            return new TerrariumCubeWorldType(this);
        }
        return new TerrariumWorldType(this);
    }
}
