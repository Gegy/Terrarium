package net.gegy1000.terrarium.server.world.pipeline.composer.biome;

import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

public interface BiomeComposer {
    Biome[] composeBiomes(ColumnData data, ChunkPos columnPos);
}
