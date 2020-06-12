package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

@Cancelable
public final class ClassifyBiomeEvent extends Event {
    private final TerrariumWorld terrarium;
    private final GrowthPredictors predictors;

    private Biome biome;

    public ClassifyBiomeEvent(TerrariumWorld terrarium, GrowthPredictors predictors) {
        this.terrarium = terrarium;
        this.predictors = predictors;
    }

    public TerrariumWorld getTerrarium() {
        return this.terrarium;
    }

    public GrowthPredictors getPredictors() {
        return this.predictors;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    @Nullable
    public Biome getBiome() {
        return this.biome;
    }
}
