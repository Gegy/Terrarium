package net.gegy1000.terrarium.server.world;

import com.google.common.base.Strings;
import net.gegy1000.cubicglue.api.CubicChunkGenerator;
import net.gegy1000.cubicglue.api.CubicWorldType;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.world.chunk.ComposableBiomeProvider;
import net.gegy1000.terrarium.server.world.chunk.ComposableCubeGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

public abstract class TerrariumWorldType implements CubicWorldType {
    private final String name;
    private final ResourceLocation identifier;
    private final ResourceLocation presetIdentifier;

    private final TerrariumCustomization customization;

    public TerrariumWorldType(String name, ResourceLocation identifier, ResourceLocation presetIdentifier) {
        this.name = Terrarium.MODID + "." + name;
        this.identifier = identifier;
        this.presetIdentifier = presetIdentifier;
        this.customization = this.buildCustomization();
    }

    public abstract TerrariumGeneratorInitializer createGeneratorInitializer(World world, GenerationSettings settings);

    public abstract TerrariumDataInitializer createDataInitializer(World world, GenerationSettings settings);

    public abstract Collection<ICapabilityProvider> createCapabilities(World world, GenerationSettings settings);

    public abstract PropertyPrototype buildPropertyPrototype();

    public abstract TerrariumCustomization buildCustomization();

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CubicChunkGenerator createGenerator(World world) {
        return new ComposableCubeGenerator(world);
    }

    @Override
    public BiomeProvider createBiomeProvider(World world) {
        if (!world.isRemote) {
            return new ComposableBiomeProvider(world);
        }
        return new BiomeProviderSingle(Biomes.DEFAULT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomize(Minecraft client, WorldType worldType, GuiCreateWorld parent) {
        TerrariumPreset preset = this.getPreset();
        if (preset == null) {
            Terrarium.LOGGER.warn("Found no preset with id {} for world type {}", this.presetIdentifier, this.getName());
            return;
        }

        TerrariumCustomizationGui customizationGui = this.createCustomizationGui(parent, worldType, preset);
        if (Strings.isNullOrEmpty(parent.chunkProviderSettingsJson)) {
            client.displayGuiScreen(new SelectPresetGui(customizationGui, parent, this));
        } else {
            client.displayGuiScreen(customizationGui);
        }
    }

    @SideOnly(Side.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(GuiCreateWorld parent, WorldType worldType, TerrariumPreset preset) {
        return new TerrariumCustomizationGui(parent, worldType, this, preset);
    }

    @Override
    public final boolean isCustomizable() {
        return !this.customization.getCategories().isEmpty() && this.getPreset() != null;
    }

    @Override
    public final int calculateMaxGenerationHeight(WorldServer world) {
        if (world.provider.getDimensionType() == DimensionType.OVERWORLD) {
            GenerationSettings settings = GenerationSettings.parse(world);
            return this.calculateMaxGenerationHeight(world, settings);
        }
        return 256;
    }

    protected int calculateMaxGenerationHeight(WorldServer world, GenerationSettings settings) {
        return Short.MAX_VALUE;
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    public TerrariumPreset getPreset() {
        return TerrariumPresetRegistry.get(this.presetIdentifier);
    }

    public TerrariumCustomization getCustomization() {
        return this.customization;
    }

    public boolean isHidden() {
        return false;
    }
}
