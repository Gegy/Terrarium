package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class ConfigureShrubsEvent extends Event {
    private final Cover cover;
    private final GrowthPredictors predictors;
    private final TreeDecorator.Builder builder;

    public ConfigureShrubsEvent(Cover cover, GrowthPredictors predictors, TreeDecorator.Builder builder) {
        this.cover = cover;
        this.predictors = predictors;
        this.builder = builder;
    }

    public Cover getCover() {
        return this.cover;
    }

    public GrowthPredictors getPredictors() {
        return this.predictors;
    }

    public TreeDecorator.Builder getBuilder() {
        return this.builder;
    }
}
