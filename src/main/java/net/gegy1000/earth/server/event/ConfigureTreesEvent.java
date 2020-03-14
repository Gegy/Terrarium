package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.composer.EarthTreeComposer;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class ConfigureTreesEvent extends Event {
    private final Cover cover;
    private final GrowthPredictors predictors;
    private final EarthTreeComposer.Builder builder;

    public ConfigureTreesEvent(Cover cover, GrowthPredictors predictors, EarthTreeComposer.Builder builder) {
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

    public EarthTreeComposer.Builder getBuilder() {
        return this.builder;
    }
}
