package net.gegy1000.terrarium.server.world;

import com.google.common.base.Strings;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.chunk.ComposableBiomeProvider;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.chunk.TerrariumChunkGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class TerrariumWorldType extends WorldType {
    private static Field nameField;

    private final ResourceLocation identifier;
    private final ResourceLocation presetIdentifier;
    private final TerrariumCustomization customization;

    static {
        try {
            nameField = ReflectionHelper.findField(WorldType.class, "name", "field_77133_f");
            if (nameField != null) {
                nameField.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);
            } else {
                Terrarium.LOGGER.error("Failed to find world type name field");
            }
        } catch (ReflectiveOperationException e) {
            Terrarium.LOGGER.error("Failed to get world type name field", e);
        }
    }

    public TerrariumWorldType(String name, ResourceLocation identifier, ResourceLocation presetIdentifier) {
        super("length_bypass");
        setName(this, Terrarium.MODID + "." + name);
        this.identifier = identifier;
        this.presetIdentifier = presetIdentifier;
        this.customization = this.buildCustomization();
    }

    private static void setName(WorldType worldType, String name) {
        if (nameField != null) {
            try {
                nameField.set(worldType, name);
            } catch (IllegalAccessException e) {
                Terrarium.LOGGER.error("Failed to set world type name", e);
            }
        }
    }

    public abstract TerrariumGeneratorInitializer createInitializer(World world, TerrariumChunkGenerator chunkGenerator, GenerationSettings settings);

    protected abstract TerrariumCustomization buildCustomization();

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
        TerrariumPreset preset = this.getPreset();
        if (preset == null) {
            Terrarium.LOGGER.warn("Found no preset with id {} for world type {}", this.presetIdentifier, this.getName());
            return;
        }

        TerrariumCustomizationGui customizationGui = this.createCustomizationGui(parent, preset);
        if (Strings.isNullOrEmpty(parent.chunkProviderSettingsJson)) {
            mc.displayGuiScreen(new SelectPresetGui(customizationGui, this));
        } else {
            mc.displayGuiScreen(customizationGui);
        }
    }

    @SideOnly(Side.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(GuiCreateWorld parent, TerrariumPreset preset) {
        return new TerrariumCustomizationGui(parent, this, preset);
    }

    @Override
    public final boolean isCustomizable() {
        return !this.customization.getCategories().isEmpty() && this.getPreset() != null;
    }

    public boolean isHidden() {
        return false;
    }

    public final TerrariumWorldData getWorldData(World world) {
        TerrariumWorldData worldData = world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (worldData == null) {
            throw new IllegalStateException("Terrarium world capability not yet present");
        }
        return worldData;
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
}
