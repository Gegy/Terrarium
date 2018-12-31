package net.gegy1000.terrarium.server.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.customization.TerrariumPresetRegistry;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import javax.annotation.Nullable;

public abstract class TerrariumGeneratorType<C extends TerrariumGeneratorConfig> extends CustomLevelGenerator {
    private final Identifier identifier;
    private final Identifier presetIdentifier;
    private final TerrariumCustomization customization;

    public TerrariumGeneratorType(String name, Identifier identifier, Identifier presetIdentifier) {
        super(name);
        this.identifier = identifier;
        this.presetIdentifier = presetIdentifier;
        this.customization = this.buildCustomization();

        this.toggleStructures = false;
    }

    public abstract ChunkGenerator<C> createGenerator(World world, GenerationSettings settings, GenerationContext context);

    public abstract PropertyPrototype buildPropertyPrototype();

    protected abstract TerrariumCustomization buildCustomization();

    @Override
    @Nullable
    @Environment(EnvType.CLIENT)
    public final Gui createCustomizationGui(NewLevelGui parent) {
        TerrariumPreset preset = this.getPreset();
        if (preset == null) {
            Terrarium.LOGGER.warn("Found no preset with id {} for world type {}", this.presetIdentifier, this.getName());
            return null;
        }

        TerrariumCustomizationGui customizationGui = this.createCustomizationGui(parent, preset);
        CompoundTag settingsTag = parent.field_3200;
        if (settingsTag == null || settingsTag.isEmpty()) {
            return (new SelectPresetGui(customizationGui, this));
        } else {
            return customizationGui;
        }
    }

    @Environment(EnvType.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(NewLevelGui parent, TerrariumPreset preset) {
        return new TerrariumCustomizationGui(parent, this, preset);
    }

    @Override
    public final boolean isCustomizable() {
        return !this.customization.getCategories().isEmpty() && this.getPreset() != null;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public TerrariumPreset getPreset() {
        return TerrariumPresetRegistry.INSTANCE.get(this.presetIdentifier);
    }

    public TerrariumCustomization getCustomization() {
        return this.customization;
    }
}
