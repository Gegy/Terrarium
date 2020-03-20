package net.gegy1000.terrarium.server.event;

import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;

public class TerrariumInitializeDataEvent extends TerrariumInitializeEvent {
    private final DataGenerator.Builder dataGenerator;

    public TerrariumInitializeDataEvent(
            World world, TerrariumWorldType worldType,
            GenerationSettings settings,
            DataGenerator.Builder dataGenerator
    ) {
        super(world, worldType, settings);
        this.dataGenerator = dataGenerator;
    }

    public DataGenerator.Builder getDataGenerator() {
        return this.dataGenerator;
    }
}
