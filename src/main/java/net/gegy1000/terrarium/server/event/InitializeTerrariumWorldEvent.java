package net.gegy1000.terrarium.server.event;

import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.data.ColumnDataGenerator;
import net.gegy1000.terrarium.server.world.generator.CompositeTerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class InitializeTerrariumWorldEvent extends Event {
    private final World world;
    private final TerrariumWorldType worldType;
    private final GenerationSettings settings;

    private final CompositeTerrariumGenerator.Builder generator;
    private final ColumnDataGenerator.Builder dataGenerator;

    public InitializeTerrariumWorldEvent(
            World world, TerrariumWorldType worldType,
            GenerationSettings settings,
            CompositeTerrariumGenerator.Builder generator,
            ColumnDataGenerator.Builder dataGenerator
    ) {
        this.world = world;
        this.worldType = worldType;
        this.settings = settings;
        this.generator = generator;
        this.dataGenerator = dataGenerator;
    }

    public World getWorld() {
        return this.world;
    }

    public TerrariumWorldType getWorldType() {
        return this.worldType;
    }

    public GenerationSettings getSettings() {
        return this.settings;
    }

    public CompositeTerrariumGenerator.Builder getGenerator() {
        return this.generator;
    }

    public ColumnDataGenerator.Builder getDataGenerator() {
        return this.dataGenerator;
    }
}
