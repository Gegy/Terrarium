package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

import javax.annotation.Nullable;

public class TerrariumGeneratorConfig extends ChunkGeneratorSettings {
    private final GenerationSettings settings;
    private final RegionGenerationHandler regionHandler;
    private final ChunkCompositionProcedure<?> compositionProcedure;
    private final Coordinate spawnPosition;

    public TerrariumGeneratorConfig(
            GenerationSettings settings,
            RegionGenerationHandler regionHandler,
            ChunkCompositionProcedure<?> compositionProcedure,
            Coordinate spawnPosition
    ) {
        this.settings = settings;
        this.regionHandler = regionHandler;
        this.compositionProcedure = compositionProcedure;
        this.spawnPosition = spawnPosition;
    }

    @Nullable
    public static TerrariumGeneratorConfig get(IWorld world) {
        ChunkGenerator<?> generator = world.getChunkManager().getChunkGenerator();
        if (generator == null) {
            return null;
        }

        ChunkGeneratorSettings config = generator.getSettings();
        if (config instanceof TerrariumGeneratorConfig) {
            return (TerrariumGeneratorConfig) config;
        }

        return null;
    }

    public GenerationSettings getSettings() {
        return this.settings;
    }

    public RegionGenerationHandler getRegionHandler() {
        return this.regionHandler;
    }

    @SuppressWarnings("unchecked")
    public <C extends TerrariumGeneratorConfig> ChunkCompositionProcedure<C> getCompositionProcedure() {
        return (ChunkCompositionProcedure<C>) this.compositionProcedure;
    }

    public Coordinate getSpawnPosition() {
        return this.spawnPosition;
    }
}
