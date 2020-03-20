package net.gegy1000.terrarium.server.event;

import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class TerrariumInitializeEvent extends Event {
    private final World world;
    private final TerrariumWorldType worldType;
    private final GenerationSettings settings;

    protected TerrariumInitializeEvent(
            World world, TerrariumWorldType worldType,
            GenerationSettings settings
    ) {
        this.world = world;
        this.worldType = worldType;
        this.settings = settings;
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
}
