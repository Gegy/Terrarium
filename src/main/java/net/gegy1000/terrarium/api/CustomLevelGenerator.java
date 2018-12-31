package net.gegy1000.terrarium.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.world.IWorld;
import net.minecraft.world.level.LevelGeneratorType;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Random;

public abstract class CustomLevelGenerator {
    protected final String name;
    protected boolean visible = true;
    protected boolean hidden = false;
    protected boolean toggleStructures = true;
    protected boolean customizable = false;

    private LevelGeneratorType generatorType;

    protected CustomLevelGenerator(String name) {
        this.name = name;
    }

    public final LevelGeneratorType create() {
        try {
            Constructor<LevelGeneratorType> constructor = LevelGeneratorType.class.getDeclaredConstructor(int.class, String.class);
            constructor.setAccessible(true);

            this.generatorType = constructor.newInstance(generateId(), this.name);
            ((ExtendedLevelGenerator) this.generatorType).setSource(this);

            return this.generatorType;
        } catch (Throwable t) {
            throw new RuntimeException("Cannot create generator type", t);
        }
    }

    public final LevelGeneratorType getGenerator() {
        if (this.generatorType == null) {
            throw new IllegalStateException("Generator not registered");
        }
        return this.generatorType;
    }

    @Nullable
    public static CustomLevelGenerator unwrap(LevelGeneratorType generatorType) {
        if (generatorType == null) {
            return null;
        }
        ExtendedLevelGenerator extended = (ExtendedLevelGenerator) generatorType;
        return extended.getSource();
    }

    private static int generateId() {
        for (int i = 0; i < LevelGeneratorType.TYPES.length; i++) {
            if (LevelGeneratorType.TYPES[i] == null) {
                return i;
            }
        }
        throw new RuntimeException("No free generator ids");
    }

    public String getName() {
        return this.name;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean canToggleStructures() {
        return this.toggleStructures;
    }

    public boolean isCustomizable() {
        return this.customizable;
    }

    public boolean shouldReduceSlimeSpawns(IWorld world, Random random) {
        return false;
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    public Gui createCustomizationGui(NewLevelGui parent) {
        return null;
    }
}
