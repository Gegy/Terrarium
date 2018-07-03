package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

public final class AttachedComponent<T> {
    private final RegionComponentType<T> type;
    private final RegionPopulator<T> populator;

    public AttachedComponent(RegionComponentType<T> type, RegionPopulator<T> populator) {
        this.type = type;
        this.populator = populator;
    }

    public RegionComponentType<T> getType() {
        return this.type;
    }

    public RegionComponent<T> createAndPopulate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        T data = this.populator.populate(settings, pos, regionSize, width, height);
        return new RegionComponent<>(this.type, data);
    }
}
