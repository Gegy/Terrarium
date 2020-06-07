package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class ConfigureTreesEvent extends Event {
    private final TerrariumWorld terrarium;
    private final Cover cover;
    private final GrowthPredictors predictors;
    private final TreeDecorator.Builder builder;

    public ConfigureTreesEvent(TerrariumWorld terrarium, Cover cover, GrowthPredictors predictors, TreeDecorator.Builder builder) {
        this.terrarium = terrarium;
        this.cover = cover;
        this.predictors = predictors;
        this.builder = builder;
    }

    public TerrariumWorld getTerrarium() {
        return this.terrarium;
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
