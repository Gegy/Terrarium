package net.gegy1000.terrarium.server.event;

import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.generator.CompositeTerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;

public class TerrariumInitializeGeneratorEvent extends TerrariumInitializeEvent {
    private final CompositeTerrariumGenerator.Builder generator;
    private final ColumnDataCache dataCache;

    public TerrariumInitializeGeneratorEvent(
            World world, TerrariumWorldType worldType,
            GenerationSettings settings,
            CompositeTerrariumGenerator.Builder generator,
            ColumnDataCache dataCache
    ) {
        super(world, worldType, settings);
        this.generator = generator;
        this.dataCache = dataCache;
    }

    public CompositeTerrariumGenerator.Builder getGenerator() {
        return this.generator;
    }

    public ColumnDataCache getDataCache() {
        return this.dataCache;
    }
}
