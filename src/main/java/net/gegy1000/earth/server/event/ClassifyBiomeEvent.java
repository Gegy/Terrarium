package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

@Cancelable
public final class ClassifyBiomeEvent extends Event {
    private final TerrariumWorld terrarium;
    private final BiomeClassifier.Context context;

    private Biome biome;

    public ClassifyBiomeEvent(TerrariumWorld terrarium, BiomeClassifier.Context context) {
        this.terrarium = terrarium;
        this.context = context;
    }

    public TerrariumWorld getTerrarium() {
        return this.terrarium;
    }

    public BiomeClassifier.Context getContext() {
        return this.context;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    @Nullable
    public Biome getBiome() {
        return this.biome;
    }
}
